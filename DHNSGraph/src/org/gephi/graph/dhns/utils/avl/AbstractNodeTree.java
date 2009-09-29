/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.utils.avl;

import java.util.Iterator;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 *
 * @author Mathieu
 */
public class AbstractNodeTree {

    protected AbstractNodeAVLNode root;
    protected int count;

    public boolean add(AbstractNode item) {
        AbstractNodeAVLNode p = this.root;

        if (p == null) {
            this.root = new AbstractNodeAVLNode(item);
        } else {
            while (true) {
                int c = item.getNumber() - p.item.getNumber();

                if (c < 0) {
                    if (p.left != null) {
                        p = p.left;
                    } else {
                        p.left = new AbstractNodeAVLNode(item, p);
                        p.balance--;

                        break;
                    }
                } else if (c > 0) {
                    if (p.right != null) {
                        p = p.right;
                    } else {
                        p.right = new AbstractNodeAVLNode(item, p);
                        p.balance++;

                        break;
                    }
                } else {
                    return false;
                }
            }

            while ((p.balance != 0) && (p.parent != null)) {
                if (p.parent.left == p) {
                    p.parent.balance--;
                } else {
                    p.parent.balance++;
                }

                p = p.parent;

                if (p.balance == -2) {
                    AbstractNodeAVLNode x = p.left;

                    if (x.balance == -1) {
                        x.parent = p.parent;

                        if (p.parent == null) {
                            this.root = x;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = x;
                            } else {
                                p.parent.right = x;
                            }
                        }

                        p.left = x.right;

                        if (p.left != null) {
                            p.left.parent = p;
                        }

                        x.right = p;
                        p.parent = x;

                        x.balance = 0;
                        p.balance = 0;
                    } else {
                        AbstractNodeAVLNode w = x.right;

                        w.parent = p.parent;

                        if (p.parent == null) {
                            this.root = w;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = w;
                            } else {
                                p.parent.right = w;
                            }
                        }

                        x.right = w.left;

                        if (x.right != null) {
                            x.right.parent = x;
                        }

                        p.left = w.right;

                        if (p.left != null) {
                            p.left.parent = p;
                        }

                        w.left = x;
                        w.right = p;

                        x.parent = w;
                        p.parent = w;

                        if (w.balance == -1) {
                            x.balance = 0;
                            p.balance = 1;
                        } else if (w.balance == 0) {
                            x.balance = 0;
                            p.balance = 0;
                        } else // w.balance == 1
                        {
                            x.balance = -1;
                            p.balance = 0;
                        }

                        w.balance = 0;
                    }

                    break;
                } else if (p.balance == 2) {
                    AbstractNodeAVLNode x = p.right;

                    if (x.balance == 1) {
                        x.parent = p.parent;

                        if (p.parent == null) {
                            this.root = x;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = x;
                            } else {
                                p.parent.right = x;
                            }
                        }

                        p.right = x.left;

                        if (p.right != null) {
                            p.right.parent = p;
                        }

                        x.left = p;
                        p.parent = x;

                        x.balance = 0;
                        p.balance = 0;
                    } else {
                        AbstractNodeAVLNode w = x.left;

                        w.parent = p.parent;

                        if (p.parent == null) {
                            this.root = w;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = w;
                            } else {
                                p.parent.right = w;
                            }
                        }

                        x.left = w.right;

                        if (x.left != null) {
                            x.left.parent = x;
                        }

                        p.right = w.left;

                        if (p.right != null) {
                            p.right.parent = p;
                        }

                        w.right = x;
                        w.left = p;

                        x.parent = w;
                        p.parent = w;

                        if (w.balance == 1) {
                            x.balance = 0;
                            p.balance = -1;
                        } else if (w.balance == 0) {
                            x.balance = 0;
                            p.balance = 0;
                        } else // w.balance == -1
                        {
                            x.balance = 1;
                            p.balance = 0;
                        }

                        w.balance = 0;
                    }

                    break;
                }
            }
        }

        this.count++;
        return true;
    }

    public boolean remove(AbstractNode item) {
        return this.remove(item.getNumber());
    }

    public boolean remove(int number) {
        AbstractNodeAVLNode p = this.root;

        while (p != null) {
            int c = number - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                AbstractNodeAVLNode y; // node from which rebalancing begins

                int choice = 0; 		//0:Done  1:Left  2:Right

                if (p.right == null) // Case 1: p has no right child
                {
                    if (p.left != null) {
                        p.left.parent = p.parent;
                    }

                    if (p.parent == null) {
                        this.root = p.left;

                        count--;
                        return true;
                    }

                    if (p == p.parent.left) {
                        p.parent.left = p.left;

                        y = p.parent;

                        choice = 1;
                    // goto LeftDelete;
                    } else {
                        p.parent.right = p.left;

                        y = p.parent;

                        choice = 2;
                    //goto RightDelete;
                    }
                } else if (p.right.left == null) // Case 2: p's right child has no left child
                {
                    if (p.left != null) {
                        p.left.parent = p.right;
                        p.right.left = p.left;
                    }

                    p.right.balance = p.balance;
                    p.right.parent = p.parent;

                    if (p.parent == null) {
                        this.root = p.right;
                    } else {
                        if (p == p.parent.left) {
                            p.parent.left = p.right;
                        } else {
                            p.parent.right = p.right;
                        }
                    }

                    y = p.right;

                    choice = 2;
                //goto RightDelete;
                } else // Case 3: p's right child has a left child
                {
                    AbstractNodeAVLNode s = p.right.left;

                    while (s.left != null) {
                        s = s.left;
                    }

                    if (p.left != null) {
                        p.left.parent = s;
                        s.left = p.left;
                    }

                    s.parent.left = s.right;

                    if (s.right != null) {
                        s.right.parent = s.parent;
                    }

                    p.right.parent = s;
                    s.right = p.right;

                    y = s.parent; // for rebalacing, must be set before we change s.parent

                    s.balance = p.balance;
                    s.parent = p.parent;

                    if (p.parent == null) {
                        this.root = s;
                    } else {
                        if (p == p.parent.left) {
                            p.parent.left = s;
                        } else {
                            p.parent.right = s;
                        }
                    }

                    choice = 1;
                // goto LeftDelete;
                }

                // rebalancing begins
                while (choice != 0) {
                    if (choice == 1) {
                        //LeftDelete:

                        y.balance++;

                        if (y.balance == 1) {
                            //goto Done;
                            choice = 0;
                        } else if (y.balance == 2) {
                            AbstractNodeAVLNode x = y.right;

                            if (x.balance == -1) {
                                AbstractNodeAVLNode w = x.left;

                                w.parent = y.parent;

                                if (y.parent == null) {
                                    this.root = w;
                                } else {
                                    if (y.parent.left == y) {
                                        y.parent.left = w;
                                    } else {
                                        y.parent.right = w;
                                    }
                                }

                                x.left = w.right;

                                if (x.left != null) {
                                    x.left.parent = x;
                                }

                                y.right = w.left;

                                if (y.right != null) {
                                    y.right.parent = y;
                                }

                                w.right = x;
                                w.left = y;

                                x.parent = w;
                                y.parent = w;

                                if (w.balance == 1) {
                                    x.balance = 0;
                                    y.balance = -1;
                                } else if (w.balance == 0) {
                                    x.balance = 0;
                                    y.balance = 0;
                                } else // w.balance == -1
                                {
                                    x.balance = 1;
                                    y.balance = 0;
                                }

                                w.balance = 0;

                                y = w; // for next iteration
                            } else {
                                x.parent = y.parent;

                                if (y.parent != null) {
                                    if (y.parent.left == y) {
                                        y.parent.left = x;
                                    } else {
                                        y.parent.right = x;
                                    }
                                } else {
                                    this.root = x;
                                }

                                y.right = x.left;

                                if (y.right != null) {
                                    y.right.parent = y;
                                }

                                x.left = y;
                                y.parent = x;

                                if (x.balance == 0) {
                                    x.balance = -1;
                                    y.balance = 1;

                                    //goto Done
                                    choice = 0;
                                } else {
                                    x.balance = 0;
                                    y.balance = 0;

                                    y = x; // for next iteration
                                }
                            }
                        }
                    } else if (choice == 2) {
                        //goto LoopTest;


                        //RightDelete:

                        y.balance--;

                        if (y.balance == -1) {
                            choice = 0;
                        //goto Done;
                        } else if (y.balance == -2) {
                            AbstractNodeAVLNode x = y.left;

                            if (x.balance == 1) {
                                AbstractNodeAVLNode w = x.right;

                                w.parent = y.parent;

                                if (y.parent == null) {
                                    this.root = w;
                                } else {
                                    if (y.parent.left == y) {
                                        y.parent.left = w;
                                    } else {
                                        y.parent.right = w;
                                    }
                                }

                                x.right = w.left;

                                if (x.right != null) {
                                    x.right.parent = x;
                                }

                                y.left = w.right;

                                if (y.left != null) {
                                    y.left.parent = y;
                                }

                                w.left = x;
                                w.right = y;

                                x.parent = w;
                                y.parent = w;

                                if (w.balance == -1) {
                                    x.balance = 0;
                                    y.balance = 1;
                                } else if (w.balance == 0) {
                                    x.balance = 0;
                                    y.balance = 0;
                                } else // w.balance == 1
                                {
                                    x.balance = -1;
                                    y.balance = 0;
                                }

                                w.balance = 0;

                                y = w; // for next iteration
                            } else {
                                x.parent = y.parent;

                                if (y.parent != null) {
                                    if (y.parent.left == y) {
                                        y.parent.left = x;
                                    } else {
                                        y.parent.right = x;
                                    }
                                } else {
                                    this.root = x;
                                }

                                y.left = x.right;

                                if (y.left != null) {
                                    y.left.parent = y;
                                }

                                x.right = y;
                                y.parent = x;

                                if (x.balance == 0) {
                                    x.balance = 1;
                                    y.balance = -1;

                                    choice = 0;
                                //goto Done;
                                } else {
                                    x.balance = 0;
                                    y.balance = 0;

                                    y = x; // for next iteration
                                }
                            }
                        }
                    }


                    if (choice == 0) {
                        this.count--;
                        return true;
                    }

                    //LoopTest: {

                    if (y.parent != null) {
                        if (y == y.parent.left) {
                            y = y.parent;
                            choice = 1;
                        // goto LeftDelete;
                        } else {
                            y = y.parent;
                            choice = 2;
                        //goto RightDelete;
                        }
                    } else {
                        //Done
                        this.count--;
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public boolean contains(AbstractNode item) {
        AbstractNodeAVLNode p = this.root;

        while (p != null) {
            int c = item.getNumber() - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                return true;
            }
        }

        return false;
    }

    public AbstractNode get(int number) {
        AbstractNodeAVLNode p = this.root;

        while (p != null) {
            int c = number - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                return p.item;
            }
        }

        return null;
    }

    public void clear() {
        this.root = null;
        this.count = 0;
    }

    public AbstractNodeIterator iterator() {
        return new AbstractNodeAVLIterator(this);
    }

    public int getCount() {
        return count;
    }

    private class AbstractNodeAVLNode {

        AbstractNodeAVLNode parent;
        AbstractNodeAVLNode left;
        AbstractNodeAVLNode right;
        int balance;
        AbstractNode item;

        public AbstractNodeAVLNode(AbstractNode item) {
            this.item = item;
        }

        public AbstractNodeAVLNode(AbstractNode item, AbstractNodeAVLNode parent) {
            this.item = item;
            this.parent = parent;
        }
    }

    private class AbstractNodeAVLIterator extends AbstractNodeIterator implements Iterator<AbstractNode>, ResetableIterator {

        private AbstractNodeAVLNode next;
        private AbstractNode current;

        public AbstractNodeAVLIterator() {
        }

        public AbstractNodeAVLIterator(AbstractNodeAVLNode node) {
            this.next = node;
            goToDownLeft();
        }

        public AbstractNodeAVLIterator(AbstractNodeTree tree) {
            this(tree.root);
        }

        public void setNode(AbstractNodeTree tree) {
            this.next = tree.root;
            goToDownLeft();
        }

        private void goToDownLeft() {
            if (next != null) {
                while (next.left != null) {
                    next = next.left;
                }
            }
        }

        public boolean hasNext() {
            if (next == null) {
                return false;
            }

            current = this.next.item;

            if (next.right == null) {
                while ((next.parent != null) && (next == next.parent.right)) {
                    this.next = this.next.parent;
                }

                this.next = this.next.parent;
            } else {
                this.next = this.next.right;

                while (this.next.left != null) {
                    this.next = this.next.left;
                }
            }

            return true;
        }

        public AbstractNode next() {
            return current;
        }

        public void remove() {
            AbstractNodeTree.this.remove(current);
        }
    }
}
