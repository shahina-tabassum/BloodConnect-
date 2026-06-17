package com.bloodconnect.util;

import java.util.Collections;
import java.util.List;

/**
 * Fixed list of supported cities for dropdown selects.
 * Used on register.jsp, donor-dashboard.jsp, and request-form.jsp
 * to ensure donors and requesters pick from the same set,
 * preventing silent matching failures from free-text mismatches.
 */
public class CityList {

    public static final List<String> CITIES = Collections.unmodifiableList(List.of(
        "Ahmedabad",
        "Bangalore",
        "Chennai",
        "Delhi",
        "Hyderabad",
        "Jaipur",
        "Kanpur",
        "Kolkata",
        "Lucknow",
        "Mumbai",
        "Nagpur",
        "Patna",
        "Pune",
        "Surat",
        "Visakhapatnam"
    ));
}
