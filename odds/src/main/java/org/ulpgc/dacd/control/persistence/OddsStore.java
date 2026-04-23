package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.Odd;
import java.util.List;

public interface OddsStore {
    void save(List<Odd> odds);
}
