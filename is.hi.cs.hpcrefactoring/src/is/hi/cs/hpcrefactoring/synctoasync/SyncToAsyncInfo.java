package is.hi.cs.hpcrefactoring.synctoasync;

import java.util.function.Predicate;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.core.resources.IFile;

public class SyncToAsyncInfo {
	private int offset;
	private String newName;
	private String oldName;
	private String name="";
	private MethodContext methodContext;
	private Predicate<String> nameUsedChecker = (String) -> false;
	private IFile sourceFile;
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(final int offset){
		this.offset = offset;
	}
	
	public String getNewName() {
		return newName;
	}
	
	public void setNewName(final String newName){
		this.newName = newName;
	}
	
	public String getOldName() {
		return oldName;
	}
	
	public void setOldName(final String oldName){
		this.oldName = oldName;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public MethodContext getMethodContext() {
		return methodContext;
	}
	public void setMethodContext(MethodContext context) {
		methodContext = context;
	}
	
	public IFile getSourceFile(){
		return sourceFile;
	}
	
	public void setNameUsedChecker(Predicate<String> nameOccupiedChecker) {
		this.nameUsedChecker = nameOccupiedChecker;
	}

	public boolean isNameUsed(String name) {
		return nameUsedChecker.test(name);
	}
	
	public VisibilityEnum getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityEnum visibility) {
		this.visibility = visibility;
	}

}
