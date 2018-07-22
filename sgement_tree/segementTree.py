class Node(object):
    def __init__(self,start,end):
        self.start = start
        self.end = end
        self.sum = 0
        self.left = None
        self.right = None

class segmentTree(object):
    def __init__(self,nums):
        self.root = self.createTree(nums,0,len(nums)-1)

    def createTree(self,nums,l,r):
        if l==r:
            node = Node(l,r)
            node.sum = nums[l]
            return node
        if l>r: return None
        mid = (l+r)//2
        root = Node(l,r)
        root.left = self.createTree(nums,l,mid)
        root.right = self.createTree(nums,mid+1,r)
        root.sum = root.right.sum + root.left.sum
        return root

    def updateVal(self,ind,val):
        self.updateTree(self.root,ind,val)

    def updateTree(self,root,ind,val):
        if root.start == root.end:
            root.sum = val
            return val
        mid = (root.start+root.end)//2
        if ind<=mid:
            self.updateTree(root.left,ind,val)
        else:
            self.updateTree(root.right,ind,val)
        root.sum = root.left.sum + root.right.sum
        return root.sum

    def sumRange(self, i, j):
        return self.sumRangeTree(self.root,i,j)

    def sumRangeTree(self,root,i,j):
        if root.start==i and root.end==j:
            return root.sum
        mid = (root.start+root.end)//2
        if i>=mid+1:
            return self.sumRangeTree(root.right,i,j)
        elif j<=mid:
            return self.sumRangeTree(root.left,i,j)
        else:
            return self.sumRangeTree(root.left,i,mid)+self.sumRangeTree(root.right,mid+1,j)

    def printTree(self):
        l = [self.root]
        while l:
            p = ""
            for i in l:
                s,e,v = i.start,i.end,i.sum
                p+="["+str(s)+","+str(e)+"]"+"sum:"+str(v)+"  "
            print (p)
            sub = []
            for n in l:
                if n.left:sub.append(n.left)
                if n.right:sub.append(n.right)
            l = sub

l = [11,22,32,43,54,6,17,48,93,110]
test = segmentTree(l)
print(test.sumRange(0,1))
test.updateVal(4,5)
print(test.sumRange(0,5))
test.printTree()