package de.prob.clistarter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

import de.prob.clistarter.annotations.Home;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;

public class ModuleCli extends AbstractModule {

	@Override
	protected void configure() {
		bind(ProBInstance.class).toProvider(ProBInstanceProvider.class);
		bind(OsSpecificInfo.class).toProvider(OsInfoProvider.class).asEagerSingleton();
	}

	@Provides
	@Home
	private static String getProBDirectory() {
		String homedir = System.getProperty("prob.home");
		if (homedir != null) {
			return homedir + File.separator;
		}
		return Installer.DEFAULT_HOME + File.separator;
	}

	@Provides
	@OsName
	private static String getOsName() {
		return System.getProperty("os.name");
	}

	@Provides
	@OsArch
	private static String getOsArch() {
		return System.getProperty("os.arch");
	}

	@Provides
	@DebuggingKey
	private static String createDebuggingKey() {
		Random random;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			random = new Random();
		}
		return Long.toHexString(random.nextLong());
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface OsName {
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface DebuggingKey {
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@BindingAnnotation
	@interface OsArch {
	}

}
