ALTER TABLE becpg_property MODIFY prop_id VARCHAR(62) NULL;
ALTER TABLE becpg_property MODIFY prop_name VARCHAR(255) NOT NULL;
ALTER TABLE becpg_datalist MODIFY datalist_name VARCHAR(255)  NOT NULL;
ALTER TABLE becpg_datalist MODIFY item_type VARCHAR(255)  NOT NULL;
ALTER TABLE becpg_entity   MODIFY entity_type VARCHAR(255)  NOT NULL;

CREATE INDEX prop_name_idx     ON becpg_property(prop_name);
CREATE INDEX datalist_name_idx ON becpg_datalist(datalist_name);
CREATE INDEX entity_type_idx   ON becpg_entity(entity_type);
CREATE INDEX item_type_idx     ON becpg_datalist(item_type);
CREATE INDEX prop_id_ix        ON becpg_property(prop_id); 
				
				
