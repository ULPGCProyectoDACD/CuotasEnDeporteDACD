package org.ulpgc.dacd.frontend.model;

import java.util.List;

public record FilterOptionsDTO(
        List<String> teams,
        List<String> bookmakers
) {}
