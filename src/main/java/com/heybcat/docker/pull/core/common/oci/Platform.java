package com.heybcat.docker.pull.core.common.oci;

/**
 * @author Fetters
 */
public class Platform {

    private String os;

    private String architecture;

    public Platform(){

    }

    public Platform(String os, String architecture){
        this.os = os;
        this.architecture = architecture;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }
}
