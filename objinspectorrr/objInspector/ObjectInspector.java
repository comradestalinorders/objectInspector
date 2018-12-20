package net.is_bg.ltf;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * SnapShot of the currently inspected object by the object inspector!!!
 * @author lubo
 *
 */
class SnapShot{
	private Object currentObject;                              //pointer to the currently inspected object
	private SnapShot parent;                                   //pointer to the parent object (container object) snapshot
	private int  currentInsepectedClassIndex = 0;       	   //the index in classes list  of the currently inspected class in the class hierarchy
	@SuppressWarnings("rawtypes")
	private List<Class> classes = new ArrayList<Class>();      //list containing the class hierarchy of the currently inspected object in the SnapShot
	
	/**package private constructor*/
	SnapShot(){
		
	}
	
	void setCurrentObject(Object currentObject) {
		this.currentObject = currentObject;
	}
	
	Object getCurrentObject() {
		return currentObject;
	}
	
	void setCurrentInsepectedClassIndex(int currentInsepectedClassIndex) {
		this.currentInsepectedClassIndex = currentInsepectedClassIndex;
	}
	
	int getCurrentInsepectedClassIndex() {
		return currentInsepectedClassIndex;
	}
	
	void incrementCurrentInsepectedClassIndex(){
		currentInsepectedClassIndex++;
	}
	void decrementCurrentInsepectedClassIndex(){
		currentInsepectedClassIndex--;
	}
	
	Class getCurrentObjectClass(){
		return  currentObject.getClass();
	}
	
	List<Class> getClassesList() {
		return classes;
	}
	
	void setParent(SnapShot parent) {
		this.parent = parent;
	}
	
	public SnapShot getParent() {
		return parent;
	}
	
	
	
	Class 	getCurrentlyInspectedClass(){
		return classes.get(currentInsepectedClassIndex);
	}
	
	/***
	 * Fills the classes list with the classes in the current object class hierarchy up to the Object class!!!
	 */
	void fillClassHierarchy(){
		if(currentObject == null) return;
		classes.clear();
		currentInsepectedClassIndex = 0;
		Class currentObjectClass = currentObject.getClass();
		while(currentObjectClass!=null){
			classes.add(currentObjectClass);
			currentObjectClass = currentObjectClass.getSuperclass();
		}
	}
}


/***
 * Object inspector that inspects object properties, methods, super classes!!!
 * @author lubo
 *
 */
public class ObjectInspector implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2173644984877817589L;
	private SnapShot currentSnapShot;
	
	/***
	 * Creates an Object inspector!
	 * @param objectToInspect
	 */
	public ObjectInspector(Object objectToInspect){
		 setObjectToInspect(objectToInspect);
	}
	
	/***
	 * Returns the object to be inspected!
	 * @return
	 */
	public Object getObjectToInspect(){
		return currentSnapShot.getCurrentObject();
	}
	
	/***
	 * Sets object to be  inspected!
	 * @param objectToInspect
	 */
	public void setObjectToInspect(Object objectToInspect){
		if(objectToInspect == null) return;
		SnapShot p = new SnapShot();
		p.setCurrentObject(objectToInspect);
		if(currentSnapShot == null) {
			currentSnapShot = p;
		}else{
			//add new  entry to the current snapshot
			p.setParent(currentSnapShot);
			currentSnapShot = p;
		}
		currentSnapShot.fillClassHierarchy();
		/*currentSnapShot.getClassesList().clear();
		currentSnapShot.getClassesList().add(currentSnapShot.getCurrentObjectClass());
		currentSnapShot.setCurrentInsepectedClassIndex(0);*/
	}
	
	
	public int getCurrentInsepectedClassIndex() {
		return currentSnapShot.getCurrentInsepectedClassIndex();
	}
	
	public void setCurrentInsepectedClassIndex(int currentInsepectedClassIndex) {
		currentSnapShot.setCurrentInsepectedClassIndex(currentInsepectedClassIndex);
	}
	
	/***
	 * Returns the class hierarchy list!
	 * @return
	 */
	public List<Class> getClassHierarchy(){
		return currentSnapShot.getClassesList();
	}
	
	/***
	 * Returns a list of Data Fields Wrappers for a Class! 
	 * @param c
	 * @return
	 */
	private List<DataField> getDeclaredFields(Class c){
		Field [] ff = c.getDeclaredFields();
		List<DataField> df = new ArrayList<>();
		for(Field f:ff){
			f.setAccessible(true);
			df.add(new DataField(f, currentSnapShot.getCurrentObject()));
		}
		return df;
	}
	

	
	/***
	 * Returns the name of currently inspected class!
	 * @return
	 */
	public String getCurrentInspectedClassName(){
		return currentSnapShot.getCurrentlyInspectedClass().getName();
	}
	
	/***
	 * Returns the data Fields in the currently inspected object class!!!
	 * @return
	 */
	public List<DataField> getDeclaredFields(){
		return getDeclaredFields(currentSnapShot.getCurrentlyInspectedClass());
	}
	
	/***
	 * Inspects the super class of  this object class
	 * Going up the class hierarchy tree!
	 */
	public void superClass(){
		Class superclass = currentSnapShot.getCurrentlyInspectedClass().getSuperclass();
		if(superclass != null && (currentSnapShot.getClassesList().size() <= (currentSnapShot.getCurrentInsepectedClassIndex()+1))) {currentSnapShot.getClassesList().add(superclass); }
		if(currentSnapShot.getCurrentInsepectedClassIndex() < currentSnapShot.getClassesList().size()-1) currentSnapShot.incrementCurrentInsepectedClassIndex();
	}
	
	/***
	 * Returns the to previously stored object class in the object class hierarchy
	 * Going down the class hierarchy tree!
	 */
	public void getSubClass(){
		if(currentSnapShot.getCurrentInsepectedClassIndex() <=0) return ;
		currentSnapShot.decrementCurrentInsepectedClassIndex();
	}
	
	/***
	 * Returns to the parent / container object snapshot  if any!!
	 */
	public void toParentSnapShot(){
		//if there is parent snapshot set the current snap shot to parent snapshot
		if(currentSnapShot.getParent() != null){
			SnapShot p = currentSnapShot;
			currentSnapShot = p.getParent();
			p = null;
		}
	}
	
	/***
	 * Check if the currently inspected object is sublcass of array or Collection!!!
	 * @return
	 */
	public boolean isIterable(){
		return isArray() || isCollection();
	}
	
	/***
	 * Check if the currently inspected object is sublcass of array!
	 * @return
	 */
	public boolean isArray(){
		if(currentSnapShot.getCurrentObject() == null) return false;
		return currentSnapShot.getCurrentObject().getClass().isArray();
	}
	
	
	/***
	 * Check if the currently inspected object is sublcass ofCollection!!!
	 * @return
	 */
	public boolean isCollection(){
		return currentSnapShot.getCurrentObject() instanceof Collection;
	}
	
	
	/***
	 * Casts the currently inspected object to Collection!
	 * @return
	 */
	public Collection getObjectAsCollection(){
		return (Collection)currentSnapShot.getCurrentObject();
	}
	

	
	
	
	/**
	 * A wrapper for class Field!!!
	 * @author lubo
	 *
	 */
	public static class DataField {
		private Field dataField;
		private Object object;
		
		DataField (Field dataField, Object obj){
			this.dataField = dataField;
			this.object = obj;
		}
		
		public Field getField(){
			return dataField;
		}
		
		public boolean isArray(){
			return (dataField.getType().isArray());
		}
		
		public boolean isCollection() throws IllegalArgumentException, IllegalAccessException{
			return getValue() instanceof Collection;
		}
		
		public Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException{
			if(obj == null) return null;
			return dataField.get(obj);
		}
		
		public Object getValue() throws IllegalArgumentException, IllegalAccessException{
			return getValue(object);
		}
		
		public boolean isIterable() throws IllegalArgumentException, IllegalAccessException{
			return isArray() || isCollection();
		}
		
	}
}
