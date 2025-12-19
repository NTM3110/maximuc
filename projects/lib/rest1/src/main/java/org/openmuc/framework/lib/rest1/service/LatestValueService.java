package org.openmuc.framework.lib.rest1.service;

import java.util.Map;
import org.openmuc.framework.lib.rest1.domain.dto.StringDetailDTO;
import org.openmuc.framework.lib.rest1.domain.dto.Account;

public interface LatestValueService {
    public Map<String, String> getDevValues();
    public String getSiteName();
    public StringDetailDTO getStringDetails(String stringId);
    public boolean deleteString(String stringId);
    public Account getAccountDetails(int accountID);
}
