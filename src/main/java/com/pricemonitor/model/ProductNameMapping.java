package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_name_mappings")
@Data
public class ProductNameMapping {

	@Id
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "external_name")
	private String externalName;

	@Column(name = "source")
	private String source;
}
