package org.jenkinsci.test.acceptance.utils;

import java.io.File;

public class MavenLocalRepository
{

    private final File mavenLocalRepository;

    private MavenLocalRepository() {

        if(System.getProperty( "mavenRepoPath" ) != null) {
            mavenLocalRepository =new File(System.getProperty( "mavenRepoPath" ));
        } else {
            File userHome = new File(System.getProperty("user.home"));
            mavenLocalRepository = new File(new File(userHome, ".m2"), "repository");
        }
    }

    private static class LazyHolder {
        static final MavenLocalRepository INSTANCE = new MavenLocalRepository();
    }

    public static File getMavenLocalRepository() {
        return LazyHolder.INSTANCE.mavenLocalRepository;
    }


}
