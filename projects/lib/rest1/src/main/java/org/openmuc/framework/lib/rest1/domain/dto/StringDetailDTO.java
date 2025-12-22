package org.openmuc.framework.lib.rest1.domain.dto;



public class StringDetailDTO {
    String stringName;
    String cellBrand;
    String cellModel;
    Double cellQty;
    Double cNominal;
    Double vNominal;

    // Getters and Setters
    public String getStringName() {
        return stringName;
    }
    public void setStringName(String stringName) {
        this.stringName = stringName;
    }
    public String getCellBrand() {
        return cellBrand;
    }
    public void setCellBrand(String cellBrand) {
        this.cellBrand = cellBrand;
    }
    public String getCellModel() {
        return cellModel;
    }
    public void setCellModel(String cellModel) {
        this.cellModel = cellModel;
    }
    public Double getCellQty() {
        return cellQty;
    }
    public void setCellQty(Double cellQty) {
        this.cellQty = cellQty;
    }
    public Double getCNominal() {
        return cNominal;
    }
    public void setCNominal(Double cNominal) {
        this.cNominal = cNominal;
    }
    public Double getVNominal() {
        return vNominal;
    }
    public void setVNominal(Double vNominal) {
        this.vNominal = vNominal;
    }
}
