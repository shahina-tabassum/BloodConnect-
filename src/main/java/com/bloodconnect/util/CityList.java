package com.bloodconnect.util;

import java.util.Collections;
import java.util.List;

/**
 * Fixed list of supported cities for dropdown selects.
 * Used on register.html, donor-dashboard.html, and request-form.html
 * to ensure donors and requesters pick from the same set,
 * preventing silent matching failures from free-text mismatches.
 */
public class CityList {

    public static final List<String> CITIES = Collections.unmodifiableList(List.of(
        "Ahmedabad",
        "Bangalore",
        "Bhopal",
        "Chandigarh",
        "Chennai",
        "Coimbatore",
        "Delhi",
        "Gurgaon",
        "Hyderabad",
        "Indore",
        "Jaipur",
        "Kanpur",
        "Kochi",
        "Kolkata",
        "Lucknow",
        "Mumbai",
        "Nagpur",
        "Noida",
        "Patna",
        "Pune",
        "Surat",
        "Visakhapatnam"
    ));
}

