package com.workpool.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {
    private double latitude;
    private double longitude;
    private String city;
    private String district;
    private String state;
    private String pincode;
}
