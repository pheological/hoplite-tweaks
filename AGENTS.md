# Release and handoff policy

These instructions apply to every task in this repository, in addition to any
instructions inherited from parent directories.

## GitHub after changes

- After completing and verifying any repository change, commit all files that
  belong to that change and push the commit to the repository's GitHub `main`
  branch.
- Fetch first and integrate remote changes safely. Never force-push.
- Do not include unrelated user changes in the commit.
- Report the pushed commit hash and provide a clickable GitHub commit link.

## Modrinth releases require owner approval

- Do not publish a new Modrinth version merely because code was changed or
  pushed.
- After a release-worthy change is built and verified, ask the repository owner
  for explicit approval to publish it to Modrinth. Publishing may proceed
  without an additional question when the owner's current request already
  explicitly says to publish or upload that specific change to Modrinth.
- Before publishing, increment the patch version unless the owner specifies a
  different version, update the changelog, and run the complete release build
  and test suite.
- Publish only the Minecraft versions currently approved by the owner. Do not
  publish a Minecraft 26.3 build unless the owner explicitly asks for it.
- Verify every created Modrinth version by its returned version ID and provide
  links to the published versions.
- Treat Modrinth tokens as secrets and never repeat them in user-facing output.

## JAR handoff

- After every successful build, list each relevant distributable JAR as a
  clickable Markdown link using its absolute local filesystem path.
- Link only runnable release JARs by default, not sources or Javadoc JARs.
- On a desktop host, also reveal the directory containing the JARs in Finder
  when permitted, so the owner can drag the files into another application.
- Keep the links in the final response even when the JAR directory was revealed
  in Finder.
