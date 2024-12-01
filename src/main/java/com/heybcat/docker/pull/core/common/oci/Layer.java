package com.heybcat.docker.pull.core.common.oci;

import com.heybcat.docker.pull.core.common.enums.MediaType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Fetters
 */
@Setter
@Getter
public class Layer {

    private MediaType mediaType;

    private String digest;

    private Long size;

}
