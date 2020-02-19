package at.goasystems.verstor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Verstor {

	private static final Logger logger = LoggerFactory.getLogger(Verstor.class);
	private static final String ERROR = "Error";
	private Gson gson;

	public Verstor() {
		this.gson = new Gson();
	}

	public Git createRepository() {

		/* Create repository directory. */
		String temporaryId = getNewUniqueId();
		File directory = new File(System.getProperty("java.io.tmpdir"), temporaryId);
		directory.mkdir();

		/* Create repository. */
		Git git = null;
		try {
			git = Git.init().setDirectory(directory).call();
		} catch (IllegalStateException | GitAPIException e) {
			logger.error(ERROR, e);
		}

		return git;
	}

	public Git addResource(Git git, Resource resource) {

		try {
			File workingdir = git.getRepository().getDirectory().getParentFile();
			File newresdir = new File(workingdir, resource.getResourceid());
			for (LocalizedFile localizedfile : resource.getFiles()) {
				FileUtils.copyFile(localizedfile.getFile(), new File(newresdir, localizedfile.getIsocode()));
			}
			FileUtils.writeStringToFile(new File(newresdir, "metadata"), this.gson.toJson(resource.getMetadata()),
					StandardCharsets.UTF_8);
			git.add().addFilepattern(newresdir.getName()).call();
			git.commit().setMessage(String.format("Directory %s added.", newresdir.getName())).call();
		} catch (GitAPIException | IOException e) {
			logger.error("Error adding files.", e);
		}
		return git;
	}

	public String getNewUniqueId() {
		return Long.toString(new GregorianCalendar().getTimeInMillis());
	}

	public void exportFileFromBranch(Git git, String targetbranch, String file, OutputStream exportto) {
		Repository repository = git.getRepository();
		TreeWalk treeWalk = null;
		try (RevWalk revWalk = new RevWalk(repository)) {
			ObjectId lastCommitId = repository.resolve(targetbranch);
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			treeWalk.setFilter(PathFilter.create(file));
			if (treeWalk.next()) {
				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);
				loader.copyTo(exportto);
			}
			revWalk.dispose();
		} catch (IOException e) {
			logger.error(ERROR, e);
		} finally {
			if (treeWalk != null) {
				treeWalk.close();
			}
		}
	}
}
