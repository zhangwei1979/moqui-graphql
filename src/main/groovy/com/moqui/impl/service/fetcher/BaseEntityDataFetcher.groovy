package com.moqui.impl.service.fetcher

import org.moqui.context.ExecutionContextFactory
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.moqui.impl.entity.EntityDefinition
import org.moqui.util.MNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.moqui.impl.service.GraphQLSchemaDefinition.FieldDefinition

class BaseEntityDataFetcher extends BaseDataFetcher {

    protected final static Logger logger = LoggerFactory.getLogger(BaseEntityDataFetcher.class)

    String entityName, interfaceEntityName, operation
    String requireAuthentication
    String interfaceEntityPkField
    List<String> pkFieldNames = new ArrayList<>(1)
    String fieldRawType
    Map<String, String> relKeyMap = new HashMap<>()

    BaseEntityDataFetcher (MNode node, FieldDefinition fieldDef, ExecutionContextFactory ecf) {
        super(fieldDef, ecf)

        Map<String, String> keyMap = new HashMap<>()
        for (MNode keyMapNode in node.children("key-map"))
            keyMap.put(keyMapNode.attribute("field-name"), keyMapNode.attribute("related") ?: keyMapNode.attribute("field-name"))

        initializeFields(node.attribute("entity-name"), node.attribute("interface-entity-name"), keyMap)
    }

    BaseEntityDataFetcher (ExecutionContextFactory ecf, FieldDefinition fieldDef, String entityName, Map<String, String> relKeyMap ) {
        this(ecf, fieldDef, entityName, null, relKeyMap)
    }

    BaseEntityDataFetcher (ExecutionContextFactory ecf, FieldDefinition fieldDef, String entityName,
                           String interfaceEntityName, Map<String, String> relKeyMap) {
        super(fieldDef, ecf)
        initializeFields(entityName, interfaceEntityName, relKeyMap)
    }

    private void initializeFields(String entityName, String interfaceEntityName, Map<String, String> relKeyMap) {
        this.requireAuthentication = fieldDef.requireAuthentication ?: "true"
        this.entityName = entityName
        this.interfaceEntityName = interfaceEntityName
        this.fieldRawType = fieldDef.type
        this.relKeyMap.putAll(relKeyMap)
        if ("true".equals(fieldDef.isList)) this.operation = "list"
        else this.operation = "one"

        if (interfaceEntityName) {
            EntityDefinition ed = ((ExecutionContextFactoryImpl) ecf).entityFacade.getEntityDefinition(interfaceEntityName)
            if (ed.getFieldNames(true, false).size() != 1)
                throw new IllegalArgumentException("Entity ${interfaceEntityName} for interface should have one primary key")
            interfaceEntityPkField = ed.getFieldNames(true, false).first()
        }

        EntityDefinition ed = ((ExecutionContextFactoryImpl) ecf).entityFacade.getEntityDefinition(entityName)
        pkFieldNames.addAll(ed.pkFieldNames)
    }
}
