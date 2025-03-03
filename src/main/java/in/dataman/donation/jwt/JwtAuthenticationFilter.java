package in.dataman.donation.jwt;

import java.io.IOException;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Autowired
	private JwtHelper jwtHelper;

	@Autowired
	private SessionManagementService sessionManagementService;

	@Autowired
	private UserDetailsService userDetailsService;

	private static final int SC_TOO_MANY_REQUESTS = 429;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestHeader = request.getHeader("Authorization");

		if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
			String token = requestHeader.substring(7);
			try {
				String username = jwtHelper.getClaimFromToken(token, Claims::getSubject);
				System.out.println(username);//ashwani.pandey@dataman.in
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					String userId = jwtHelper.getClaimFromToken(token, claims -> claims.get("userId", String.class));
					System.out.println(userId);
					if (!sessionManagementService.validateSession(userId, token)) {
						logger.error("Invalid session for userId: {}", userId);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.getWriter().write("Invalid session. Please log in again.");
						return;
					}

					if (jwtHelper.isRateLimited(userId)) {
						logger.warn("Rate limit exceeded for userId: {}", userId);
						response.setStatus(SC_TOO_MANY_REQUESTS);
						response.getWriter().write("Rate limit exceeded");
						return;
					}
					 
					System.out.println("I am about to load user...!"); // this is printed

					UserDetails userDetails = userDetailsService.loadUserByUsername(username); // error 
					System.out.println(userDetails);
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception e) {
				logger.error("Token validation error: {}", e.getMessage());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Invalid token"); //Token validation error: ashwani.pandey@dataman.in
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}

//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);
//
//    @Autowired
//    private JwtHelper jwtHelper;
//
//    @Autowired
//    private SessionManagementService sessionManagementService;
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    private final int SC_TOO_MANY_REQUESTS = 429;
//
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String requestHeader = request.getHeader("Authorization");
//
//        String username = null;
//        String token = null;
//
//        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
//            token = requestHeader.substring(7);
//            System.out.println("This is username extract form token"+ jwtHelper.getUsernameFromToken(token));
//            try {
//                username = jwtHelper.getUsernameFromToken(token);
//                logger.debug("This is username extract form token: {}",username);
//            } catch (Exception e) {
//                logger.error("Error extracting username from token: {}", e.getMessage());
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid token");
//                return;
//            }
//        }
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            String userId = jwtHelper.getClaimFromToken(token, claims -> claims.get("userId", String.class));
//
//            // Validate the session
//            if (!sessionManagementService.validateSession(userId, token)) {
//                logger.error("Session validation failed for userId: {}", userId);
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.getWriter().write("Invalid session. Please log in again.");
//                return;
//            }
//
//            // Rate limiting
//            if (jwtHelper.isRateLimited(userId)) {
//                logger.warn("Rate limit exceeded for userId: {}", userId);
//                response.setStatus(SC_TOO_MANY_REQUESTS);
//                response.getWriter().write("Rate limit exceeded");
//                return;
//            }
//
//            if (jwtHelper.validateToken(token)) {
//                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } else {
//                logger.error("Token validation failed for userId: {}", userId);
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid token");
//                return;
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
