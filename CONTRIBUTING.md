If you have an idea for the app you are very welcome to implement it yourself!

To make it easier for me to read and understand your pull requests please follow the upcoming guidelines as closely as possible.

## Guidelines

- Adapt to the code style of the repository. Its the standard Java style:
  camelCase for variables and PascalCase for classes. Constants are capitalized.

- The variable names should be comprehensive and the code self-explanatory. Use comments only where required for structure, comprehension or warning.

- Avoid suppressing warnings by annotations, this only hides potential issues. If the IDE raises a warning either fix it or explain why you cant in a comment.

- Commits should have a clear commit message prefaced with a keyword of what has been done.
  Examples:   
  - fix: error where xyz fails when calling abc
  - implementation: view for the object xyz

- Every commit should be able to compile, so the changes made in it should be complete. If not that should be indicated in the commit message. 
  The completing commit should then mention the commit it completes.

- Every commit should be as concise and small as possible within the bounds of the rule above. Every commit should be its own logical and consistent unit of change.

- Please do not change and commit the gradle files unless you need to add a new dependency or it is inevitable for your change to work.

- Your pull request should already have been properly tested by yourself.



Thank you for helping me with the project!