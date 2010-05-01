package net.ishchenko.omfp;

import java.util.List;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 27.03.2010
 * Time: 5:52:02
 */
public class ContextSensitiveIteration {

    private Stack<Integer> indexesStack = new Stack<Integer>();
    private Stack<Integer> sizesStack = new Stack<Integer>();

    public <E> void iterate(List<E> elements, Procedure<E> runnable) {
        sizesStack.push(elements.size());
        indexesStack.push(0);
        for (; getIndex() < elements.size(); indexesStack.push(indexesStack.pop() + 1)) {
            runnable.go(elements.get(getIndex()));
        }
        indexesStack.pop();
        sizesStack.pop();
    }

    public boolean isFirst() {
        return getIndex() == 0;
    }

    public boolean isLast() {
        return getIndex() == sizesStack.peek() - 1;
    }

    private int getIndex() {
        return indexesStack.peek();
    }


}
