package com.drajer.test.util;

import java.util.HashMap;
import java.util.Map;

public class ValueSetMapping {

  public static HashMap<String, String> telecomUseCodes;

  static {
    telecomUseCodes = new HashMap<>();
    telecomUseCodes.put("HOME", "HP");
    telecomUseCodes.put("WORK", "WP");
    telecomUseCodes.put("MOBILE", "MC");
  }

  public static final Map<String, String> sectionConversion;

  static {
    sectionConversion = new HashMap<>();
    sectionConversion.put("PROBLEMS", "11450-4");
    sectionConversion.put("ENCOUNTERS", "46240-8");
    sectionConversion.put("RESULTS", "30954-2");
    sectionConversion.put("MEDICATIONS", "29549-3");
    sectionConversion.put("IMMUNIZATIONS", "11369-6");
    sectionConversion.put("HISTORY", "29762-2");
    sectionConversion.put("ILLNESS", "10164-2");
    sectionConversion.put("VISITS", "29299-5");
    sectionConversion.put("TREATMENTS", "18776-5");
  }
}
