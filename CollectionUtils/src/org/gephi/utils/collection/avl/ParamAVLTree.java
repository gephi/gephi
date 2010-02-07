package org.gephi.utils.collection.avl;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Special type of AVL tree which possess a {@link AVLItemAccessor}. It allows to configure the indexes returned
 * by the tree nodes
 * 
 * @author Mathieu Bastian
 * @param <Item> The type of Object in the tree
 */
public class ParamAVLTree<Item> implements Iterable<Item> {

    protected ParamAVLNode<Item> root;
    protected int count;
    private AVLItemAccessor<Item> accessor;

    public ParamAVLTree(AVLItemAccessor<Item> accessor) {
        this.accessor = accessor;
    }

    public ParamAVLTree() {
    }

    public boolean add(Item item) {
        ParamAVLNode<Item> p = this.root;

        if (p == null) {
            this.root = new ParamAVLNode(item);
        } else {
            while (true) {
                int c = accessor.getNumber(item) - accessor.getNumber(p.item);

                if (c < 0) {
                    if (p.left != null) {
                        p = p.left;
                    } else {
                        p.left = new ParamAVLNode(item, p);
                        p.balance--;

                        break;
                    }
                } else if (c > 0) {
                    if (p.right != null) {
                        p = p.right;
                    } else {
                        p.right = new ParamAVLNode(item, p);
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
                    ParamAVLNode x = p.left;

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
                        ParamAVLNode w = x.right;

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
                    ParamAVLNode x = p.right;

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
                        ParamAVLNode w = x.left;

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

    public boolean remove(Item item) {
        ParamAVLNode<Item> p = this.root;

        while (p != null) {
            int c = accessor.getNumber(item) - accessor.getNumber(p.item);

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                ParamAVLNode y; // node from which rebalancing begins

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
                    ParamAVLNode s = p.right.left;

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
                            ParamAVLNode x = y.right;

                            if (x.balance == -1) {
                                ParamAVLNode w = x.left;

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
                            ParamAVLNode x = y.left;

                            if (x.balance == 1) {
                                ParamAVLNode w = x.right;

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

    public boolean contains(Item item) {
        ParamAVLNode<Item> p = this.root;

        while (p != null) {
            int c = accessor.getNumber(item) - accessor.getNumber(p.item);

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

    public Item getItem(int number) {
        ParamAVLNode<Item> p = this.root;

        while (p != null) {
            int c = number - accessor.getNumber(p.item);

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

    public Iterator<Item> iterator() {
        return new ParamAVLIterator(this);
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return count==0;
    }

    public AVLItemAccessor<Item> getAccessor() {
        return accessor;
    }

    public void setAccessor(AVLItemAccessor<Item> accessor) {
        this.accessor = accessor;
    }

    public Item[] toArray(Item[] array) {
        Item[] result = (Item[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), count);
        if(count==0)
            return result;
        ParamAVLIterator<Item> itr = new ParamAVLIterator<Item>(root);
        for (int i = 0; itr.hasNext(); i++) {
            Item item = itr.next();
            result[i] = item;
        }
        return result;
    }
}
