package org.pentaho.metastore.persist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD } )
public @interface MetaStoreAttribute {
  String key() default "";

  boolean password() default false;

  boolean nameReference() default false;

  String nameListKey() default "";

  boolean filenameReference() default false;

  String filenameListKey() default "";
}
