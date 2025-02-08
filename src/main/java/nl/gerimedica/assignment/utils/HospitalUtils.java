package nl.gerimedica.assignment.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HospitalUtils {

    private static int usageCounter = 0;

    public static void recordUsage(String context) {
        usageCounter++;
        log.info("HospitalUtils used. Counter: {} | Context: {}", usageCounter, context);
    }
}
