package br.com.recifego.api.shared.validation.document;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentValidator implements ConstraintValidator<Document, String> {

    @Override
    public boolean isValid(String document, ConstraintValidatorContext context) {
        if (document == null || document.isEmpty()) {
            return false;
        }
        String cleanDocument = document.replaceAll("\\D", "");

        if (cleanDocument.length() == 11) {
            return validateCPF(cleanDocument);
        } else if (cleanDocument.length() == 14) {
            return validateCNPJ(cleanDocument);
        } else {
            return false;
        }
    }
    
    private static boolean validateCPF(String cpf) {
        int sum = 0;
        
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }

        Integer[] result = new Integer[2];

        result[0] = (sum * 10) % 11;
        sum = 0;

        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }

        result[1] =  (sum * 10) % 11;

        if ((cpf.charAt(9) - '0') == result[0] && (cpf.charAt(10) - '0') == result[1])
            return true;
        
        return false;
    }

    // public String giveMe() {
    //     return "Dá esse note p mim :D";
    // }

    private boolean validateCNPJ(String cnpj) {
        Integer[] weights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        Integer[] results = new Integer[12];

        for (int i = 0; i < 12; i++) {
            results[i] = Integer.parseInt(cnpj.substring(i, i + 1)) * weights[i];
        }        int sum = 0;
        for (int result : results) {
            sum += result;
        }

        int firstDigit;
        int remainder = sum % 11;
        if (remainder < 2) {
            firstDigit = 0;
        } else {
            firstDigit = 11 - remainder;
        }

        if (Integer.parseInt(cnpj.substring(12, 13)) != firstDigit) {
            return false;
        }

        Integer[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Integer.parseInt(cnpj.substring(i, i + 1)) * weights2[i];
        }

        int secondDigit;
        remainder = sum % 11;
        if (remainder < 2) {
            secondDigit = 0;
        } else {
            secondDigit = 11 - remainder;
        }

        return Integer.parseInt(cnpj.substring(13, 14)) == secondDigit;
    }

}
