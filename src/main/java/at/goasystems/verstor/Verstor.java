package at.goasystems.verstor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
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
			git.checkout().setName("master").call();
		} catch (GitAPIException e) {
			logger.error("Were not able to checkout the master branch. Maybe it doesn't exist yet.", e);
		}

		try {
			File workingdir = git.getRepository().getDirectory().getParentFile();
			File newresdir = new File(workingdir, resource.getResourceid());
			for (LocalizedFile localizedfile : resource.getLocalizedfiles()) {
				FileUtils.copyFile(new File(localizedfile.getFile()), new File(newresdir, localizedfile.getIsocode()));
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

	public Git removeResource(Git git, String resourceid) {
		Resource resource = new Resource();
		resource.setResourceid(resourceid);
		removeResource(git, resource);
		return git;
	}

	public Git removeResource(Git git, Resource resource) {

		try {
			git.checkout().setName("master").call();
		} catch (GitAPIException e) {
			logger.error("Were not able to checkout the master branch. Maybe it doesn't exist yet.", e);
		}

		try {
			File workingdir = git.getRepository().getDirectory().getParentFile();
			FileUtils.deleteDirectory(new File(workingdir, resource.getResourceid()));
			git.rm().addFilepattern(resource.getResourceid()).call();
			git.commit().setMessage(String.format("Directory %s removed.", resource.getResourceid())).call();
		} catch (GitAPIException | IOException e) {
			logger.error("Error adding files.", e);
		}
		return git;
	}

	public String getNewUniqueId() {
		return Long.toString(new GregorianCalendar().getTimeInMillis());
	}

	public void exportFileFromBranch(Git git, String commithash, OutputStream exportto) {

		ZipOutputStream zos = new ZipOutputStream(exportto);

		Repository repository = git.getRepository();
		TreeWalk treeWalk = null;
		try (RevWalk revWalk = new RevWalk(repository)) {
			ObjectId lastCommitId = repository.resolve(commithash);
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(false);
			treeWalk.setFilter(PathFilter.create("res1"));
			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					logger.debug("dir: {}", treeWalk.getPathString());
					treeWalk.enterSubtree();
				} else {
					logger.debug("file: {}", treeWalk.getPathString());
					zos.putNextEntry(new ZipEntry(treeWalk.getPathString()));
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					loader.copyTo(zos);
					zos.closeEntry();
				}
			}
			zos.close();
			revWalk.dispose();
		} catch (IOException e) {
			logger.error(ERROR, e);
		} finally {
			if (treeWalk != null) {
				treeWalk.close();
			}
		}
	}

	public List<String> logDev(Git git) {
		List<String> commithashes = new ArrayList<>();
		try {
			ObjectId head = git.getRepository().resolve(Constants.HEAD);
			Iterable<RevCommit> commits = git.log().add(head).setMaxCount(10).call();
			logger.debug("Logs loaded.");
			for (Iterator<RevCommit> iterator = commits.iterator(); iterator.hasNext();) {
				RevCommit commit = iterator.next();
				if (commit != null && commit.getName() != null) {
					commithashes.add(commit.getName());
				}
			}
		} catch (GitAPIException | RevisionSyntaxException | IOException e) {
			logger.error(ERROR, e);
		}
		return commithashes;
	}
}
