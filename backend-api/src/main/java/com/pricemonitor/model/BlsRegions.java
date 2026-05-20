package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "bls_regions")
@Data
public class BlsRegions {

	@Id
	@Column(name = "code")
	private String code;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "bls_suffix", nullable = false)
	private String blsSuffix;

	@Column(name = "zip_prefix_ranges")
	private String zipPrefixRanges;
}
