package org.ulpgc.dacd.business.model;

import java.util.List;

public record FilterOptionsDTO(
        List<String> teams,
        List<String> bookmakers
) {}
