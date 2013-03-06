
metastore
----------
This project contains a flexible metadata, data and configuration information store. 
Anyone can use it but it was designed for use within the Pentaho software stack.

The "meta-model" is simple and very generic.
The top level entry is always a namespace. The namespace can be used by non-Pentaho companies to store their own information separate from anyone else.

The next level in the meta-model is an Element Type.  A very generic name was chosen on purpose to reflect the fact that you can store just about anything.  The element is at this point in time nothing more than a simple placeholder: an ID, a name and a description.

Finally, each element type can have a series of Elements.  
Each element has an ID and a set of key/value pairs (called "id" and "value") as child attributes. All attributes can have children of their own.
An element has security information: an owner and a set of owner-permissions describing who has which permission to use the element. (CRUD permissions)

- Namespace
   \
    --- Element Type
         \
          --- Element
               \
                --- Attributes
