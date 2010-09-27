/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.data.attributes.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * It is essentially a map from intervals to object which can be queried for
 * {@code Interval} instances associated with a particular interval of time.
 *
 * <p>Insertion can be performed in <i>O</i>(lg <i>n</i>) time, where <i>n</i>
 * is the number of nodes. All intervals in a tree that overlap some interval
 * <i>i</i> can be listed in <i>O</i>(min(<i>n</i>, <i>k</i> lg <i>n</i>) time,
 * where <i>k</i> is the number of intervals in the output list. Thus search and
 * deletion can be performed in this time.
 *
 * <p>The space consumption is <i>O</i>(<i>n</i>).
 *
 * <p>Note that this implementation doesn't allow intervals to be duplicated.
 *
 * <p>References:
 * <ul>
 * <li>Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, and Clifford Stein.
 * Introduction to Algorithms, Second Edition. MIT, 2001. ISBN 83-204-2879-3
 * </ul>
 *
 * @author Cezary Bartosiak
 *
 * @param <T> type of data
 */
public final class IntervalTree<T> {
	private Node nil;  // the sentinel node
	private Node root; // the root of this interval tree

	/**
	 * Constructs an empty {@code IntervalTree}.
	 */
	public IntervalTree() {
		nil = new Node();
		nil.left = nil.right = nil.p = nil;
		root = nil;
	}

	/**
	 * Constructs a copy of the given {@code IntervalTree}.
	 *
	 * @param intervalTree a copied {@code IntervalTree}
	 */
	public IntervalTree(IntervalTree intervalTree) {
		this();
		copy(intervalTree.root.left, intervalTree.nil);
	}

	private void copy(Node x, Node nil) {
		if (x != nil) {
			copy(x.left, nil);
			insert(x.i);
			copy(x.right, nil);
		}
	}

	private boolean compareLow(Interval a, Interval b) {
		if (a.getLow() < b.getLow() || a.getLow() == b.getLow() &&
				(!a.isLowExcluded() || b.isHighExcluded()))
			return true;
		return false;
	}

	/**
	 * Inserts the {@code interval} into this {@code IntervalTree}.
	 * 
	 * @param interval an interval to be inserted
	 * 
	 * @throws NullPointerException if {@code interval} is null.
	 */
	public void insert(Interval<T> interval) {
		if (interval == null)
			throw new NullPointerException("Interval cannot be null.");

		insert(new Node(interval));
	}

	private void insert(Node z) {
		z.left = z.right = nil;

		Node y = root;
		Node x = root.left;
		while (x != nil) {
			y = x;
			if (compareLow(z.i, y.i))
				x = x.left;
			else x = x.right;
			y.max = Math.max(z.max, y.max);
		}
		z.p = y;
		if (y == root || compareLow(z.i, y.i))
			y.left = z;
		else y.right = z;
		insertFixup(z);
	}

	private void insertFixup(Node z) {
		Node y = nil;

		z.color = RED;
		while (z.p.color == RED)
			if (z.p == z.p.p.left) {
				y = z.p.p.right;
				if (y.color == RED) {
					z.p.color   = BLACK;
					y.color     = BLACK;
					z.p.p.color = RED;
					z = z.p.p;
				}
				else {
					if (z == z.p.right) {
						z = z.p;
						leftRotate(z);
					}
					z.p.color   = BLACK;
					z.p.p.color = RED;
					rightRotate(z.p.p);
				}
			}
			else {
				y = z.p.p.left;
				if (y.color == RED) {
					z.p.color   = BLACK;
					y.color     = BLACK;
					z.p.p.color = RED;
					z = z.p.p;
				}
				else {
					if (z == z.p.left) {
						z = z.p;
						rightRotate(z);
					}
					z.p.color   = BLACK;
					z.p.p.color = RED;
					leftRotate(z.p.p);
				}
			}
		root.left.color = BLACK;
	}

	/**
	 * Removes all intervals from this {@code IntervalTree} that overlap with
	 * the given {@code interval}.
	 * 
	 * @param interval determines which intervals should be removed
	 * 
	 * @throws NullPointerException if {@code interval} is null.
	 */
	public void delete(Interval interval) {
		if (interval == null)
			throw new NullPointerException("Interval cannot be null.");

		for (Node n : searchNodes(interval))
			delete(n);
	}

	private void delete(Node z) {
		z.max = Double.NEGATIVE_INFINITY;
		for (Node i = z.p; i != root; i = i.p)
			i.max = Math.max(i.left.max, i.right.max);

		Node y;
		Node x;

		if (z.left == nil || z.right == nil)
			y = z;
		else y = succesor(z);
		if (y.left == nil)
			x = y.right;
		else x = y.left;
		x.p = y.p;
		if (root == x.p)
			root.left = x;
		else if (y == y.p.left)
			y.p.left = x;
		else y.p.right = x;
		if (y != z) {
			if (y.color == BLACK)
				deleteFixup(x);

			y.left   = z.left;
			y.right  = z.right;
			y.p      = z.p;
			y.color  = z.color;
			z.left.p = z.right.p = y;
			if (z == z.p.left)
				z.p.left = y;
			else z.p.right = y;
		}
		else if (y.color == BLACK)
			deleteFixup(x);
	}

	private void deleteFixup(Node x) {
		while (x != root.left && x.color == BLACK)
			if (x == x.p.left) {
				Node w = x.p.right;
				if (w.color == RED) {
					w.color   = BLACK;
					x.p.color = RED;
					leftRotate(x.p);
					w = x.p.right;
				}
				if (w.left.color == BLACK && w.right.color == BLACK) {
					w.color = RED;
					x = x.p;
				}
				else {
					if (w.right.color == BLACK) {
						w.left.color = BLACK;
						w.color = RED;
						rightRotate(w);
						w = x.p.right;
					}
					w.color       = x.p.color;
					x.p.color     = BLACK;
					w.right.color = BLACK;
					leftRotate(x.p);
					x = root.left;
				}
			}
			else {
				Node w = x.p.left;
				if (w.color == RED) {
					w.color   = BLACK;
					x.p.color = RED;
					rightRotate(x.p);
					w = x.p.left;
				}
				if (w.right.color == BLACK && w.left.color == BLACK) {
					w.color = RED;
					x = x.p;
				}
				else {
					if (w.left.color == BLACK) {
						w.right.color = BLACK;
						w.color = RED;
						leftRotate(w);
						w = x.p.left;
					}
					w.color      = x.p.color;
					x.p.color    = BLACK;
					w.left.color = BLACK;
					rightRotate(x.p);
					x = root.left;
				}
			}
		x.color = BLACK;
	}

	private void leftRotate(Node x) {
		Node y = x.right;

		x.right = y.left;
		if (y.left != nil)
			y.left.p = x;
		y.p = x.p;
		if (x == x.p.left)
			x.p.left = y;
		else x.p.right = y;
		y.left = x;
		x.p    = y;

		y.max = x.max;
		x.max = Math.max(x.i.getHigh(), Math.max(x.left.max, x.right.max));
	}

	private void rightRotate(Node x) {
		Node y = x.left;

		x.left = y.right;
		if (y.right != nil)
			y.right.p = x;
		y.p = x.p;
		if (x == x.p.left)
			x.p.left = y;
		else x.p.right = y;
		y.right = x;
		x.p     = y;

		y.max = x.max;
		x.max = Math.max(x.i.getHigh(), Math.max(x.left.max, x.right.max));
	}

	private Node succesor(Node x) {
		Node y = x.right;
		if (y != nil) {
			while (y.left != nil)
				y = y.left;
			return y;
		}
		y = x.p;
		while (x == y.right) {
			x = y;
			y = y.p;
		}
		if (y == root)
			return nil;
		return y;
	}

	/**
	 * Returns the interval with the lowest left endpoint.
	 *
	 * @return the interval with the lowest left endpoint
	 *         or null if the tree is empty.
	 */
	public Interval<T> minimum() {
		if (root.left == nil)
			return null;
		return treeMinimum(root.left).i;
	}

	private Node treeMinimum(Node x) {
		while (x.left != nil)
			x = x.left;
		return x;
	}

	/**
	 * Returns the interval with the highest left endpoint.
	 * 
	 * @return the interval with the highest left endpoint
	 *         or null if the tree is empty.
	 */
	public Interval<T> maximum() {
		if (root.left == nil)
			return null;
		return treeMaximum(root.left).i;
	}

	private Node treeMaximum(Node x) {
		while (x.right != nil)
			x = x.right;
		return x;
	}

	/**
	 * Returns the leftmost point or {@code Double.NEGATIVE_INFINITY} in case
	 * of no intervals.
	 *
	 * @return the leftmost point.
	 */
	public double getLow() {
		if (isEmpty())
			return Double.NEGATIVE_INFINITY;
		return minimum().getLow();
	}

	/**
	 * Returns the rightmost point or {@code Double.POSITIVE_INFINITY} in case
	 * of no intervals.
	 *
	 * @return the rightmost point.
	 */
	public double getHigh() {
		if (isEmpty())
			return Double.POSITIVE_INFINITY;
		return root.left.max;
	}

	/**
	 * Indicates if the leftmost point is excluded.
	 *
	 * @return {@code true} if the leftmost point is excluded,
	 *         {@code false} otherwise.
	 */
	public boolean isLowExcluded() {
		if (isEmpty())
			return true;
		return minimum().isLowExcluded();
	}

	/**
	 * Indicates if the rightmost point is excluded.
	 *
	 * @return {@code true} if the rightmost point is excluded,
	 *         {@code false} otherwise.
	 */
	public boolean isHighExcluded() {
		if (isEmpty())
			return true;
		return maximum().isHighExcluded();
	}

	/**
	 * Indicates if this {@code IntervalTree} contains 0 intervals.
	 * 
	 * @return {@code true} if this {@code IntervalTree} is empty,
	 *         {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return root.left == nil;
	}

	/**
	 * Returns all intervals overlapping with a given {@code Interval}.
	 *
	 * @param interval an {#code Interval} to be searched for overlaps
	 *
	 * @return all intervals overlapping with a given {@code Interval}.
	 *
	 * @throws NullPointerException if {@code interval} is null.
	 */
	public List<Interval<T>> search(Interval interval) {
		if (interval == null)
			throw new NullPointerException("Interval cannot be null.");

		List<Interval<T>> overlaps = new ArrayList<Interval<T>>();
		for (Node n : searchNodes(interval))
			overlaps.add(n.i);
		return overlaps;
	}

	/**
	 * Returns all intervals overlapping with an interval given by {@code low}
	 * and {@code high}. They are considered as included by default.
	 *
	 * @param low  the left endpoint of an interval to be searched for overlaps
	 * @param high the right endpoint an interval to be searched for overlaps
	 *
	 * @return all intervals overlapping with an interval given by {@code low}
	 * and {@code high}.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public List<Interval<T>> search(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		List<Interval<T>> overlaps = new ArrayList<Interval<T>>();
		for (Node n : searchNodes(new Interval(low, high)))
			overlaps.add(n.i);
		return overlaps;
	}

	private List<Node> searchNodes(Interval interval) {
		List<Node> result = new ArrayList<Node>();
		searchNodes(root.left, interval, result);
		return result;
	}

	private void searchNodes(Node n, Interval interval, List<Node> result) {
		// Don't search nodes that don't exist.
		if (n == nil)
			return;

		// Skip all nodes that have got their max value below the start of
		// the given interval.
		if (interval.getLow() > n.max)
			return;

		// Search left children.
		if (n.left != nil)
			searchNodes(n.left, interval, result);

		// Check this node.
		if (n.i.compareTo(interval) == 0)
			result.add(n);

		// Skip all nodes to the right of nodes whose low value is past the end
		// of the given interval.
		if (interval.compareTo(n.i) < 0)
			return;

		// Otherwise, search right children.
		if (n.right != nil)
			searchNodes(n.right, interval, result);
	}

	/**
	 * Indicates if this {@code IntervalTree} overlaps with the given time interval.
	 *
	 * @param interval a given time interval
	 *
	 * @return {@code true} if this {@code IntervalTree} overlaps with {@code interval},
	 *         {@code false} otherwise.
	 */
	public boolean overlapsWith(Interval interval) {
		return overlapsWith(root.left, interval);
	}

	private boolean overlapsWith(Node n, Interval interval) {
		// Don't search nodes that don't exist.
		if (n == nil)
			return false;

		// Skip all nodes that have got their max value below the start of
		// the given interval.
		if (interval.getLow() > n.max)
			return false;

		// Search left children.
		if (n.left != nil)
			if (overlapsWith(n.left, interval))
				return true;

		// Check this node.
		if (n.i.compareTo(interval) == 0)
			return true;

		// Skip all nodes to the right of nodes whose low value is past the end
		// of the given interval.
		if (interval.compareTo(n.i) < 0)
			return false;

		// Otherwise, search right children.
		if (n.right != nil)
			if (overlapsWith(n.right, interval))
				return true;

		// No overlaps, return false.
		return false;
	}

	private void inorderTreeWalk(Node x, List<Interval<T>> list) {
		if (x != nil) {
			inorderTreeWalk(x.left, list);
			list.add(x.i);
			inorderTreeWalk(x.right, list);
		}
	}

	/**
	 * Compares this interval tree with the specified object for equality.
	 *
	 * <p>Note that two interval trees are equal if they contain the same
	 * intervals.
	 *
	 * @param obj object to which this interval tree is to be compared
	 *
	 * @return {@code true} if and only if the specified {@code Object} is a
	 *         {@code IntervalTree} which contain the same intervals as this
	 *         {@code IntervalTree's}.
	 * 
	 * @see #hashCode
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			List<Interval<T>> thisIntervals = new ArrayList<Interval<T>>();
			List<Interval<T>> objIntervals  = new ArrayList<Interval<T>>();
			inorderTreeWalk(root.left, thisIntervals);
			((IntervalTree<T>)obj).inorderTreeWalk(
					((IntervalTree<T>)obj).root.left, objIntervals);
			if (thisIntervals.size() == objIntervals.size()) {
				for (int i = 0; i < thisIntervals.size(); ++i)
					if (!thisIntervals.get(i).equals(objIntervals.get(i)))
						return false;
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a hashcode of this interval tree.
	 * 
	 * @return a hashcode of this interval tree.
	 */
	@Override
	public int hashCode() {
		List<Interval<T>> list = new ArrayList<Interval<T>>();
		inorderTreeWalk(root.left, list);
		return Arrays.deepHashCode(list.toArray());
	}

	/**
	 * Creates a string representation of all the intervals with their values.
	 *
	 * @param timesAsDoubles indicates if times should be shown as doubles or dates
	 *
	 * @return a string representation with times as doubles or dates.
	 */
	public String toString(boolean timesAsDoubles) {
		List<Interval<T>> list = new ArrayList<Interval<T>>();
		inorderTreeWalk(root.left, list);
		if (!list.isEmpty()) {
			StringBuilder sb = new StringBuilder("<");
			sb.append(list.get(0).toString(timesAsDoubles));
			for (int i = 1; i < list.size(); ++i)
				sb.append("; ").append(list.get(i).toString(timesAsDoubles));
			sb.append(">");
			return sb.toString();
		}
		return "<empty>";
	}

	/**
	 * Returns a string representation of this interval tree in a format
	 * {@code <[low, high, value], ..., [low, high, value]>}. Nodes are visited
	 * in {@code inorder}.
	 *
	 * <p>Times are always shown as doubles.</p>
	 *
	 * @return a string representation of this interval tree.
	 */
	@Override
	public String toString() {
		return toString(true);
	}

	private class Node {
		public Interval<T> i;   // i.low is the key of this node
		public double      max; // the maximum value of any interval endpoint
								// stored in the subtree rooted at this node
		
		public Color color; // the color of this node
		public Node  left;  // the left subtree of this node
		public Node  right; // the right subtree of this node
		public Node  p;     // the parent node

		/*
		 * Constructs a sentinel node by default.
		 */
		public Node() {
			color = BLACK;
		}

		/*
		 * Constructs a new {@code Node} instance.
		 */
		public Node(Interval<T> i) {
			this();
			this.i   = i;
			this.max = i.getHigh();
		}
	}

	private enum Color {
		RED, BLACK
	}

	private static final Color RED   = Color.RED;
	private static final Color BLACK = Color.BLACK;
}
