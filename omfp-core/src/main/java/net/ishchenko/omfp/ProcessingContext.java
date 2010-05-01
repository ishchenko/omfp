package net.ishchenko.omfp;


import net.ishchenko.omfp.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 26.02.2010
 * Time: 12:50:07
 */
@SuppressWarnings({"unchecked"})
public class ProcessingContext {

    private Map<String, SectionType> links = new HashMap<String, SectionType>();
    private Map<String, FictionBook.Binary> binaries = new HashMap<String, FictionBook.Binary>();

    private Stack<Object> elementsStack = new Stack<Object>();

    private ContextSensitiveIteration iteration;

    public ProcessingContext() {
        iteration = new ContextSensitiveIteration();
    }

    public Map<String, SectionType> getLinks() {
        return links;
    }

    public Map<String, FictionBook.Binary> getBinaries() {
        return binaries;
    }

    public <T> T getTopmost(Class<T> clazz) {
        for (int i = elementsStack.size() - 1; i >= 0; i--) {
            if (elementsStack.get(i).getClass() == clazz) {
                return (T) elementsStack.get(i);
            }
        }
        return null;
    }

    public boolean stackHas(Class clazz) {
        return getTopmost(clazz) != null;
    }

    public int getStackTopOffset(Class clazz) {
        for (int i = elementsStack.size() - 1; i >= 0; i--) {
            if (elementsStack.get(i).getClass() == clazz) {
                return elementsStack.size() - 1 - i;
            }
        }
        return -1;
    }

    public int countNestingOnStack(Class clazz) {
        int result = 0;
        for (int i = elementsStack.size() - 1; i >= 0; i--) {
            if (elementsStack.get(i).getClass() == clazz) {
                result++;
            }
        }
        return result;
    }

    void pushToStack(Object o) {
        elementsStack.push(o);
    }

    void popFromStack() {
        elementsStack.pop();
    }

    public ContextSensitiveIteration getIteration() {
        return iteration;
    }
}
