package org.openmuc.framework.lib.rest1.service;

import java.util.Map;
import org.openmuc.framework.lib.rest1.domain.dto.StringDetailDTO;
import org.openmuc.framework.lib.rest1.domain.dto.Account;

public interface LatestValueService {
    Map<String, String> getDevValues();
    String getSiteName();
    StringDetailDTO getStringDetails(String stringId);
    boolean deleteString(String stringId);
    public Account getAccountDetails(int accountID);
}
