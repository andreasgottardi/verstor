package at.goasystems.verstor;

import java.io.Closeable;
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

public class Verstor implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(Verstor.class);
	private Gson gson;
	private Git git;

	private static final String GITNOTSET = "Git repository not set.";

	public Verstor() {
		this.gson = new Gson();
		this.git = null;
	}

	public Verstor(Git git) {
		this.gson = new Gson();
		this.git = git;
	}

	/**
	 * Initializes the repository
	 * 
	 * @param repositorydirectory The repository directory
	 * @return The repository or null, if the specified directory already exists.
	 */
	public Git createRepository(File repositorydirectory) {

		/* Create repository. */
		Git newgit = null;

		if (repositorydirectory.exists()) {
			logger.error("Directory already exists. Will not overwrite existing directories.");
			logger.error("Please choose a different directory.");
		} else {
			repositorydirectory.mkdirs();
			try {
				newgit = Git.init().setDirectory(repositorydirectory).call();
			} catch (IllegalStateException | GitAPIException e) {
				logger.error("Can not initialize repository.", e);
			}
		}

		return newgit;
	}

	/**
	 * Creates a new repository and uses it internally
	 * 
	 * @param repositorydirectory Directory folder
	 */
	public void createAndSetRepository(File repositorydirectory) {
		this.git = createRepository(repositorydirectory);
	}

	/**
	 * Wrapper method for addResource(Git git, Resource resource).
	 * 
	 * @param resource The resource object to add.
	 */
	public void addResource(Resource resource) {
		if (this.git != null) {
			addResource(this.git, resource);
		} else {
			logger.error(GITNOTSET);
			logger.error("Use the method addResource(Git, Resource).");
		}
	}

	/**
	 * Adds a resource the the repository. If resource already exists, a update is
	 * performed and the Git log message is set accordingly.
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
			boolean isupdate = newresdir.exists();
			for (LocalizedFile localizedfile : resource.getLocalizedfiles()) {
				FileUtils.copyFile(new File(localizedfile.getUri()), new File(newresdir, localizedfile.getIsocode()));
			}

			/* Metadata does not change during updated. That would be fatal. */
			if (!isupdate) {
				FileUtils.writeStringToFile(new File(newresdir, "metadata"), this.gson.toJson(resource.getMetadata()),
						StandardCharsets.UTF_8);
			}

			if (resource.getOrigin() != null) {
				FileUtils.copyFile(new File(resource.getOrigin()), new File(newresdir, "origin"));
			}

			git.add().addFilepattern(newresdir.getName()).call();

			String action = isupdate ? "updated" : "added";
			git.commit().setMessage(String.format("Directory %s %s.", newresdir.getName(), action)).call();

		} catch (GitAPIException | IOException e) {
			logger.error("Error adding files.", e);
		}
		return git;
	}

	/**
	 * Wrapper method for removeResource(Git git, String resourceid).
	 * 
	 * @param resourceid the resource id to remove.
	 */
	public void removeResource(String resourceid) {
		if (this.git != null) {
			removeResource(this.git, resourceid);
		} else {
			logger.error(GITNOTSET);
			logger.error("Use the method removeResource(Git, String).");
		}
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
	 * Wrapper method for exportFileFromBranch(Git, String, Resource, OutputStream)
	 * 
	 * @param commithash The commit to get the file from
	 * @param resource   The name of the resource
	 * @param exportto   The output stream to save the files to
	 */
	public void exportFileFromBranch(String commithash, Resource resource, OutputStream exportto) {
		if (this.git != null) {
			exportFileFromBranch(this.git, commithash, resource.getResourceid(), exportto);
		} else {
			logger.error(GITNOTSET);
			logger.error("Use the method exportFileFromBranch(Git, String, Resource, OutputStream).");
		}
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
	 * Wrapper method for exportFileFromBranch(Git, String, String, OutputStream)
	 * 
	 * @param commithash The commit to get the file from
	 * @param resourceid The id of the resource
	 * @param exportto   The output stream to save the files to
	 */
	public void exportFileFromBranch(String commithash, String resourceid, OutputStream exportto) {
		if (this.git != null) {
			exportFileFromBranch(this.git, commithash, resourceid, exportto);
		} else {
			logger.error(GITNOTSET);
			logger.error("Use the method exportFileFromBranch(Git, String, String, OutputStream).");
		}
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
	 * Wrapper method for getCommits(Git, String)
	 * 
	 * @param folder The resource folder name
	 * @return List of commits
	 */
	public List<Commit> getCommits(String folder) {
		if (this.git != null) {
			return getCommits(this.git, folder);
		} else {
			logger.error(GITNOTSET);
			logger.error("Use the method getCommits(Git, String).");
			return new ArrayList<>();
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

	/**
	 * Returns the used repository.
	 * 
	 * @return The embedded repository or null, if repository is not set.
	 */
	public Git getGit() {
		if (this.git != null) {
			return git;
		} else {
			logger.error(GITNOTSET);
			logger.error("This method can only be used, if embedded repository is used.");
			logger.error("Use constructor Verstor(Git).");
			return null;
		}
	}

	/**
	 * Closes the embedded repository.
	 */
	@Override
	public void close() throws IOException {
		if (this.git != null) {
			this.git.close();
		} else {
			logger.error(GITNOTSET);
			logger.error("This method can only be used, if embedded repository is used.");
			logger.error("Use constructor Verstor(Git).");
		}
	}
}
