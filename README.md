NodeBox Graphics
================
Graphics is the graphical engine powering NodeBox.

Using it in your projects
=========================
NodeBox Graphics is available in the [Maven](http://maven.apache.org/) Central Repository. Add the following to your `pom.xml` file:

    <dependency>
        <groupId>net.nodebox</groupId>
        <artifactId>graphics</artifactId>
        <version>3.0.0</version>
    </dependency>

Release on the Central Repository
=================================
* Install GPG (`brew install gpg`).
* Download your `gpg.conf`, `pubring.pgp` and `secring.pgp` in the `~/.gnuconf` directory.
* Add your Sonatype JIRA credentials to your Maven settings file (Located at `~/.m2/settings.xml`):

    <settings>
      <servers>
        <server>
          <id>sonatype-nexus-snapshot</id>
          <username>your-jira-id</username>
          <password>your-jira-password</password>
        </server>
        <server>
          <id>sonatype-nexus-staging</id>
          <username>your-jira-id</username>
          <password>your-jira-password</password>
        </server>
      </servers>
    </settings>

* Prepare the release using `mvn release:prepare`. This asks for a version, packages everything, signs it with GPG and tags the release in Git.
* Perform the release using `mvn release:perform`.  This deploys the version into Sonatype staging repository.
* Login to https://oss.sonatype.org/index.html#stagingRepositories
* Select the net.nodebox repository and "Close" it.
* Select the net.nodebox repository and "Release" it.

