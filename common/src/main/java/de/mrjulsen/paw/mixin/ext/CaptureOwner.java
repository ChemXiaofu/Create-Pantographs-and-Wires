package de.mrjulsen.paw.mixin.ext;

import org.spongepowered.asm.mixin.injection.At;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mixin injector to give access to the "Owner" of a call, i.e. foo for a call foo.bar(baz)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CaptureOwner
{
	String[] method() default {};

	At[] at();

	int require() default 1;

	int expect() default 1;

	int allow() default -1;
}
