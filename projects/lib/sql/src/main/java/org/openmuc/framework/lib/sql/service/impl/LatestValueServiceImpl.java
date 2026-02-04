package org.openmuc.framework.server.restws.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.openmuc.framework.server.restws.domain.dto.Account;
import org.openmuc.framework.server.restws.domain.dto.StringDetailDTO;
import org.openmuc.framework.server.restws.domain.model.LatestValue;
import org.openmuc.framework.server.restws.sql.LatestValueRepoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatestValueServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(LatestValueRepoImpl.class);

    Map<String, String> getDevValues() {
        List<LatestValue> devValues = LatestValueRepoImpl.findByChannelIdStartingWith("dev_");
        Map<String, String> devValueMap = parseValues(devValues);
        if (devValueMap.isEmpty()) {
            logger.warn("Dev values not found.");
            return null;
        }

        return devValueMap;
    }

    String getSiteName() {
        List<LatestValue> siteNames = LatestValueRepoImpl.findByChannelIdStartingWith("site_name");
        if (siteNames.isEmpty()) {
            logger.warn("Site name not found.");
            return null;
        }
        if (siteNames.size() > 1) {
            logger.warn("Multiple site names found.");
        }
        return siteNames.get(0).getValueString();
    }

    StringDetailDTO getStringDetails(String stringId) {
        String prefixKey = "str" + stringId;
        List<LatestValue> stringDetails = LatestValueRepoImpl.findByChannelIdStartingWith(prefixKey);
        Map<String, String> stringValueMap = parseValues(stringDetails);
        if (stringValueMap.isEmpty()) {
            logger.warn("String details not found for stringId: " + stringId);
            return null;
        }
        StringDetailDTO stringDetailDTO = new StringDetailDTO();
        stringDetailDTO.setStringName(stringValueMap.get(prefixKey + "_string_name"));
        stringDetailDTO.setCellBrand(stringValueMap.get(prefixKey + "_cell_brand"));
        stringDetailDTO.setCellModel(stringValueMap.get(prefixKey + "_cell_model"));
        stringDetailDTO.setCellQty(Double.parseDouble(stringValueMap.get(prefixKey + "_cell_qty")));
        stringDetailDTO.setCNominal(Double.parseDouble(stringValueMap.get(prefixKey + "_Cnominal")));
        stringDetailDTO.setVNominal(Double.parseDouble(stringValueMap.get(prefixKey + "_Vnominal")));
        return stringDetailDTO;
    } 
    
    public Account getAccountDetails(int accountID) {
        List<LatestValue> latestValues = LatestValueRepoImpl.findByChannelIdStartingWith("account_" + accountID);
        Map<String, String> accountValueMap = parseValues(latestValues);
        if (accountValueMap.isEmpty()) {
            logger.warn("Account details not found for accountID: " + accountID);
            return null;
        }
        Account account = new Account();
        account.setUsername(accountValueMap.get("account_" + accountID + "_username"));
        account.setPassword(accountValueMap.get("account_" + accountID + "_password"));
        return account;
    }

    boolean deleteString(String stringId) {
        String prefixKey = "str" + stringId;
        List<LatestValue> latestValues = LatestValueRepoImpl.findByChannelIdStartingWith(prefixKey);
        if (latestValues.isEmpty()) {
            logger.warn("String details not found for stringId: " + stringId);
            return false;
        }
        LatestValueRepoImpl.deleteAllByChannelIdStartingWith(prefixKey);
        return true;
    }

    private Map<String, String> parseValues(List<LatestValue> values) {
        Map<String, String> valuesMap = new TreeMap<>();

        for (LatestValue value : values) {
            if (Objects.equals(value.getValueType(), "S")) {
                valuesMap.put(value.getChannelId(), value.getValueString());
            } else if (Objects.equals(value.getValueType(), "D")) {
                valuesMap.put(value.getChannelId(), String.valueOf(value.getValueDouble()));
            } else {
                throw new RuntimeException("Unknown value type: " + value.getValueType());
            }
        }

        return valuesMap;
    }
}
