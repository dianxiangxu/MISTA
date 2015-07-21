package selftest;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;

import javax.swing.JMenu;

public class ComponentFinder {
	
	public static Component findComponent(Component component, String name) {
		return findComponent(component, null, name);
	}

	public static Component findComponent(Component container, Object componentType, String componentName) {
		if (componentName.equals(container.getName())) {
			if (componentType ==null || container.getClass()== componentType.getClass()) {
				return container; 
			}
		}
		if (container instanceof Container) {
			Component componentFound = findMenuItem(container, componentType, componentName);
			if (componentFound!=null)
				return componentFound;
			componentFound = findComponentInOwnedWindow(container, componentType, componentName);
			if (componentFound!=null)
				return componentFound;
	    }
		return null;
	}

	public static Component findMenuItem(Component container, Object componentType, String componentName) {
		Component[] children = (container instanceof JMenu) ? ((JMenu) container)
				.getMenuComponents() : ((Container) container).getComponents();
		for (int i = 0; i < children.length; ++i) {
			Component child = findComponent(children[i], componentType,
					componentName);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
	
	public static Component findComponentInOwnedWindow(Component container, Object componentType, String componentName) {
	    if (container instanceof Window) {
	    	Component[] ownedWindows = ((Window)container).getOwnedWindows(); 		   
		    for (int i = 0; i < ownedWindows.length; ++i) {
		    	Component child = findComponent(ownedWindows[i], componentType, componentName);
		        if (child != null) { 
		        	return child; 
		        }
            }
	    }
	    return null;
	}
	
	/*
	public static void printChildren(Component parent) {
		System.out.println("GUI Component: Class: " + parent.getClass() + " Name: " + parent.getName());

		if (parent!=null && parent instanceof Container) {
			Component[] children = (parent instanceof JMenu)?
		          ((JMenu)parent).getMenuComponents():
		          ((Container)parent).getComponents();		   
		    for (int i = 0; i < children.length; ++i)
		    	if (children[i]!=null)
		    		printChildren(children[i]);
	    }
	}
	*/
}
