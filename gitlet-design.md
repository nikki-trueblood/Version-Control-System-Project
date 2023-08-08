# Gitlet Design Document
author:

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

###Main:
- none
###Commit: 
- Instance Variables:
  - String uid: to identify it
  - String timestamp: to see time committed
  - String parent: String to link it to previous commit
  - Hashset blob_ids: Dictionary or Hashset mapping file names to their corresponding blob IDs 
  - String message: message associated with the commit

###Repository: 
- Instance Variables:
  - none
- Static Variables:
  - ArrayList stagingAreaAdd: ArrayList of items to add to commit
  - ArrayList stagingAreaRemove: ArrayList of items to remove from commit
  - Treemap allCommits: Hashmap of all commits with keys = UID's
  - Commit head: points to current commit
  - Commit master: points to most recent commit
  - Treemap blobs: Treemap of all blobs
  - String latestCommit: the UID of the most recent Commit made
  - Commit extraBranch: is set to null unless branch method is called in which case it creates a new pointer 


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

### Main Class:

1. Main(String[] args): The main method that takes in gitlet commands and creates a repository using the init method of
the Repository class. It calls validateArgs and then based on which command it is, it calls that method from the 
Repository class.

2. validateArgs(String[] args): Validates that the argument is a valid gitlet command and that 
it has the proper number of arguments for that command.

### Commit:

1. Constructor(String message, Commit parent): Constructs instance variables "uid", "timestamp", "message",  
"parent," and "blob_ids." If parent passed in is null, sets timestamp to 00:00:00 UTC, Thursday, January 1, 1970.

2. get_message(): Returns the message instance variable.

3. get_timestamp(): Returns the timestamp instance variable.

4. get_uid(): Returns the uid instance variable. 

5. get_parent(): Returns the parent instance variable.

6. get_blob_ids(): Returns the blob_ids instance variable.

7. add_blob(String fileName, String blobUID): Adds new fileName associated with new blob to blob_ids.

8. remove_file(String fileName): Removes the key fileName from blob_ids.

### Repository:

1. init(): Creates a commit with null parent and message "initial commit" and points both head and master to it. 

2. add(String fileName): Adds fileName to the ArrayList stagingAreaAdd if it doesn't already exist there.

3. commit(String message): Creates a new Commit object with message and latestCommit passed in as arguments. Hashes the
new Commit and adds it to the Treemap allCommits. Then serializes the Treemap.

4. rm(String fileName): Adds fileName to the ArrayList stagingAreaRemove if it doesn't already exist there.

5. log(): Prints information about each of the commits in the TreeMap allCommits on the branch that the head is at,
starting with the most recent commit.

6. global_log(): Does the same thing as log but prints information for each of the commits for allCommits, not just
those on the head's current branch.

7. find(String commit_message): Iterates through each Commit in the TreeMap "allCommits" and checks if "commit_message" is
contained within the instance variable message for each Commit.

8. status(): Prints the name of the current branch as well as the names of the files in stagingAreaAdd and stagingAreaRemove.

9. checkout(String fileName): Iterates through the "allCommits" TreeMap until it finds the latest_commit UID and sets the 
"head" instance variable to that Commit.

10. checkout(String commitID, String fileName): Iterates through the "allCommits" TreeMap until it finds the Commit with the 
commitID passed in, then sets the "head" instance variable to that Commit.

11. checkout(String branchName): unsure as of right now... will come back to asap

12. branch(String branchName): sets extraBranch instance variable to branchName commit

13. rm-branch(String branchName): sets the branchName to null

14. reset(String commit_id): Iterates through TreeMap allCommits until it finds the Commit with UID that matches commit_id.
Sets both the master and the head instance variables to that Commit.

15. merge(String branchName): We are unclear about how to proceed on this particular subsection but will circle 
back to it as soon as possible.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

We serialized the TreeMap "commits" and the TreeMap "blobs," so every time we change a file or add a new Commit,
it will save the state of the program. When we want to make a new change, we retrieve the object from the 
serialized file and continue adding to it and save it when we're done.

In our .gitlet directory, we'd have .commits and .blobs because we need the information from commits and blobs to be 
saved across multiple runs so that we can go back to checkouts.


## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

