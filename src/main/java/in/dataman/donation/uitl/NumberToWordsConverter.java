package in.dataman.donation.uitl;

public class NumberToWordsConverter {
    private static final String[] tensNames = {
        "", " Ten", " Twenty", " Thirty", " Forty", " Fifty",
        " Sixty", " Seventy", " Eighty", " Ninety"
    };

    private static final String[] numNames = {
        "", " One", " Two", " Three", " Four", " Five",
        " Six", " Seven", " Eight", " Nine", " Ten", " Eleven",
        " Twelve", " Thirteen", " Fourteen", " Fifteen",
        " Sixteen", " Seventeen", " Eighteen", " Nineteen"
    };

    private static String convertLessThanOneThousand(int number) {
        String current;

        if (number % 100 < 20) {
            current = numNames[number % 100];
            number /= 100;
        } else {
            current = numNames[number % 10];
            number /= 10;

            current = tensNames[number % 10] + current;
            number /= 10;
        }
        if (number == 0) return current.trim();
        return numNames[number] + " Hundred" + current;
    }

    public static String convert(int number) {
        if (number == 0) return "Zero";

        String result = "";

        if (number >= 100000) {
            result += convertLessThanOneThousand(number / 100000) + " Lacs ";
            number %= 100000;
        }
        if (number >= 1000) {
            result += convertLessThanOneThousand(number / 1000) + " Thousand ";
            number %= 1000;
        }
        if (number > 0) {
            result += convertLessThanOneThousand(number);
        }

        return result.trim();
    }
}

