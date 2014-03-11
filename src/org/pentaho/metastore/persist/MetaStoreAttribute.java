package org.pentaho.metastore.persist;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention( RetentionPolicy.RUNTIME )
public @interface MetaStoreAttribute {
  String key() default "";

  boolean password() default false;
}
