## A project containing some of the sub-task(checklist) utilities

### How to build and deploy
1. mvn clean package
2. Place the jar in webapp lib folder.
3. Use the required utils in your process


###Classes
1. CheckOpenSubtasksTaskListener.java -> Expression: ${checkOpenSubtasksTaskListener}. This will prevent task completion if there are open subtasks/checklists.
