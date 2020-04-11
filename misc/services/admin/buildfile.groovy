#!/usr/bin/env groovy
// the "!#/usr/bin... is just to to help IDEs, GitHub diffs, etc properly detect the language and do syntax highlighting for you.
// thx to https://github.com/jenkinsci/pipeline-examples/blob/master/docs/BEST_PRACTICES.md

// note that we set a default version for this library in jenkins, so we don't have to specify it here
@Library('misc')
import de.metas.jenkins.DockerConf
import de.metas.jenkins.Misc
import de.metas.jenkins.MvnConf

def build(final MvnConf mvnConf, final Map scmVars)
{
    	// env.MF_RELEASE_VERSION is used by spring-boot's build-info goal
    stage('Build admin')
    {
		currentBuild.description= """${currentBuild.description}<p/>
			<h2>admin</h2>
		"""
		def status = sh(returnStatus: true, script: "git diff --name-only ${scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT} ${scmVars.GIT_COMMIT} . | grep .") // see if anything at all changed in this folder
			echo "status of git dif command=${status}"
			if(scmVars.GIT_COMMIT && scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT && status != 0)
		{
			currentBuild.description= """${currentBuild.description}<p/>
			No changes happened in admin.
			"""
			echo "no changes happened in admin; skip building admin";
			return;
		}

				// update the parent pom version
      			mvnUpdateParentPomVersion mvnConf

              	// set the artifact version of everything below the pom.xml
							sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DnewVersion=${env.MF_VERSION} -DallowSnapshots=false -DgenerateBackupPoms=true -DprocessDependencies=true -DprocessParent=true -DexcludeReactor=true -Dincludes=\"de.metas*:*\" ${mvnConf.resolveParams} versions:set"
							sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true -DprocessDependencies=true -DprocessParent=true -DexcludeReactor=true -Dincludes=\"de.metas*:*\" ${mvnConf.resolveParams} versions:use-latest-versions"

							sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -Dmaven.test.failure.ignore=true ${mvnConf.resolveParams} ${mvnConf.deployParam} clean deploy"

							sh "cp target/metasfresh-admin-${env.MF_VERSION}.jar src/main/docker/metasfresh-admin.jar" // copy the file so it can be handled by the docker build
				

					
							final DockerConf dockerConf = new DockerConf(
											'metasfresh-admin', // artifactName
											env.BRANCH_NAME, // branchName
											env.MF_VERSION, // versionSuffix
											'src/main/docker') // workDir
		final String publishedDockerImageName =	dockerBuildAndPush(dockerConf)

		currentBuild.description="""${currentBuild.description}<p/>
		This build's main artifact (if not yet cleaned up) is
<ul>
<li>a docker image with name <code>${publishedDockerImageName}</code>; Note that you can also use the tag <code>${env.BRANCH_NAME}_LATEST</code></li>
</ul>
		"""
    } // stage


} 

return this