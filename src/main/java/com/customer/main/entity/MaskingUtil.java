package com.customer.main.entity;

public class MaskingUtil {
	private MaskingUtil() {}

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "INVALID_PAN";
        }
        return pan.substring(0, 3) + "*****" + pan.substring(pan.length() - 1);
    }

    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "INVALID_AADHAAR";
        }
        return "********" + aadhaar.substring(aadhaar.length() - 4);
    }
}
