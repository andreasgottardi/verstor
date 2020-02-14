package at.goasystems.verstor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;

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

public class JGitEx {

	private static final Logger logger = LoggerFactory.getLogger(JGitEx.class);

	private static final String ERROR = "Error";

	private String[] filesformaster;
	private String[] filesforbranch;

	public JGitEx() {
		filesformaster = new String[] { "master1.txt", "master2.txt", "master3.txt", "master4.txt" };
		filesforbranch = new String[] { "branch1file1.txt", "branch1file2.txt" };
	}

	public Git createTestRepo() {

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

		/* Creates the files to versionize in the directory. */
		createTestFiles(filesformaster, directory);

		/* Adds and commits each file separately. */
		try {
			if (git != null) {
				for (String file : filesformaster) {
					git.add().addFilepattern(file).call();
					git.commit().setMessage("File " + file + " added.").call();
				}
			}
		} catch (IllegalStateException | GitAPIException e) {
			logger.error(ERROR, e);
		}
		return git;
	}

	public Git createBranchAndAddFiles(Git git, String branchname) {

		try {

			git.checkout().setCreateBranch(true).setName(branchname).call();
			createTestFiles(filesforbranch, git.getRepository().getDirectory().getParentFile());

			for (String file : filesforbranch) {
				git.add().addFilepattern(file).call();
				git.commit().setMessage("File " + file + " added.").call();
			}
			git.checkout().setName("master").call();
		} catch (GitAPIException e) {
			logger.error("Error creating branch.", e);
		}
		return git;
	}

	public String getNewUniqueId() {
		return Long.toString(new GregorianCalendar().getTimeInMillis());
	}

	private void createTestFiles(String[] cpsrc, File target) {

		for (String source : cpsrc) {

			try (InputStream is = JGitEx.class.getResourceAsStream("/jgit/" + source);
					OutputStream os = new FileOutputStream(new File(target, source));) {

				byte[] buffer = new byte[1024];
				int read = is.read(buffer);
				while (read != -1) {
					os.write(buffer, 0, read);
					read = is.read(buffer);
				}

			} catch (IOException e) {
				logger.error("General_error", e);
			}
		}
	}

	public String[] getFilesForMaster() {
		return filesformaster;
	}

	public String[] getFilesForBranch() {
		return filesforbranch;
	}

	public void setFilestoversionize(String[] filestoversionize) {
		this.filesformaster = filestoversionize;
	}

	public void exportFileFromBranch(Git git, String targetbranch, File exportfile) {

		Repository repository = git.getRepository();
		FileOutputStream fos = null;

		TreeWalk treeWalk = null;

		try (RevWalk revWalk = new RevWalk(repository)) {

			ObjectId lastCommitId = repository.resolve(targetbranch);
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			treeWalk.setFilter(PathFilter.create("branch1file1.txt"));
			if (treeWalk.next()) {
				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);
				fos = new FileOutputStream(exportfile);
				loader.copyTo(fos);
			}
			revWalk.dispose();
		} catch (IOException e) {
			logger.error(ERROR, e);
		} finally {

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error(ERROR, e);
				}
			}

			if (treeWalk != null) {
				treeWalk.close();
			}
		}
	}
}
