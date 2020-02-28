package at.goasystems.verstor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
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
	private Gson gson;

	public Verstor() {
		this.gson = new Gson();
	}

	/**
	 * Initializes the repository
	 * 
	 * @param repositorydirectory The repository directory
	 * @return The repository
	 */
	public Git createRepository(File repositorydirectory) {

		if (!repositorydirectory.exists()) {
			repositorydirectory.mkdirs();
		}

		/* Create repository. */
		Git git = null;
		try {
			git = Git.init().setDirectory(repositorydirectory).call();
		} catch (IllegalStateException | GitAPIException e) {
			logger.error("Can not initialize repository.", e);
		}

		return git;
	}

	/**
	 * Adds a resource the the repository
	 * 
	 * @param git      The repository
	 * @param resource The resource to add
	 * @return The repository (fluent API)
	 */
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
				FileUtils.copyFile(new File(localizedfile.getUri()), new File(newresdir, localizedfile.getIsocode()));
			}
			FileUtils.writeStringToFile(new File(newresdir, "metadata"), this.gson.toJson(resource.getMetadata()),
					StandardCharsets.UTF_8);
			FileUtils.copyFile(new File(resource.getOrigin()), new File(newresdir, "origin"));
			git.add().addFilepattern(newresdir.getName()).call();
			git.commit().setMessage(String.format("Directory %s added.", newresdir.getName())).call();
		} catch (GitAPIException | IOException e) {
			logger.error("Error adding files.", e);
		}
		return git;
	}

	/**
	 * Wrapper method for removeResource(Git, Resource)
	 * 
	 * @param git        The repository
	 * @param resourceid The resource to remove
	 * @return The repository (fluent API)
	 */
	public Git removeResource(Git git, String resourceid) {
		Resource resource = new Resource();
		resource.setResourceid(resourceid);
		removeResource(git, resource);
		return git;
	}

	/**
	 * Removes the given resource from the repository and commits the changes.
	 * 
	 * @param git      The repository
	 * @param resource The resource to remove
	 * @return The repository (fluent API)
	 */
	public Git removeResource(Git git, Resource resource) {

		try {
			git.checkout().setName("master").call();
		} catch (GitAPIException e) {
			logger.error("Were not able to checkout the master branch. Maybe it doesn't exist yet.", e);
		}

		try {
			File workingdir = git.getRepository().getDirectory().getParentFile();
			File resourcetodelete = new File(workingdir, resource.getResourceid());
			if (resourcetodelete.exists()) {
				FileUtils.deleteDirectory(resourcetodelete);
				git.rm().addFilepattern(resource.getResourceid()).call();
				git.commit().setMessage(String.format("Directory %s removed.", resource.getResourceid())).call();
			} else {
				logger.debug("Resource {} does not exist. No deletion required.", resource.getResourceid());
			}
		} catch (GitAPIException | IOException e) {
			logger.error("Error deleting files.", e);
		}
		return git;
	}

	/**
	 * Wrapper method for exportFileFromBranch(Git, String, String, OutputStream)
	 * 
	 * @param git        The repository
	 * @param commithash The commit to get the file from
	 * @param resource   The name of the resource
	 * @param exportto   The output stream to save the files to
	 */
	public void exportFileFromBranch(Git git, String commithash, Resource resource, OutputStream exportto) {
		exportFileFromBranch(git, commithash, resource.getResourceid(), exportto);
	}

	/**
	 * Exports the subtree as zip file to the specified output stream.
	 * 
	 * @param git        The repository
	 * @param commithash The commit to get the file from
	 * @param resourceid The name of the resource
	 * @param exportto   The output stream to save the files to
	 */
	public void exportFileFromBranch(Git git, String commithash, String resourceid, OutputStream exportto) {
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
			treeWalk.setFilter(PathFilter.create(resourceid));
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
			logger.error("Error extracting files from branch to zip file.", e);
		} finally {
			if (treeWalk != null) {
				treeWalk.close();
			}
		}
	}

	/**
	 * Get the commits for the given folder
	 * 
	 * @param git    The repository
	 * @param folder The resource folder name
	 * @return List of commits
	 */
	public List<Commit> getCommits(Git git, String folder) {
		List<Commit> commithashes = new ArrayList<>();
		try {
			ObjectId head = git.getRepository().resolve(Constants.HEAD);
			LogCommand lc = git.log().add(head);
			if (folder != null && !folder.isEmpty() && ".".compareTo(folder) != 0 && "/".compareTo(folder) != 0) {
				lc = lc.addPath(folder);
			}
			Iterable<RevCommit> commits = lc.call();
			for (Iterator<RevCommit> iterator = commits.iterator(); iterator.hasNext();) {
				RevCommit commit = iterator.next();
				if (commit != null) {
					Commit c = new Commit(commit.getName(), commit.getFullMessage(),
							commit.getCommitterIdent().getWhen());
					commithashes.add(c);
				}
			}
		} catch (GitAPIException | RevisionSyntaxException | IOException e) {
			logger.error("Error collecting git commits.", e);
		}
		return commithashes;
	}
}
