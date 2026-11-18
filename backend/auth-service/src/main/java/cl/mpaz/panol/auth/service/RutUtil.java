package cl.mpaz.panol.auth.service;

/** Utilidades para RUT chileno (módulo 11). */
public final class RutUtil {

    private RutUtil() {
    }

    /** Deja solo dígitos y K, y devuelve el formato cuerpo-dv (ej. 12345678-5). */
    public static String normalizar(String rut) {
        if (rut == null) {
            return "";
        }
        String limpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();
        if (limpio.length() < 2) {
            return limpio;
        }
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    public static boolean esValido(String rut) {
        String n = normalizar(rut);
        if (!n.matches("\\d{1,8}-[0-9K]")) {
            return false;
        }
        String cuerpo = n.substring(0, n.length() - 2);
        char dv = n.charAt(n.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }
}
