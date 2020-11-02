package ark.dock;

import java.util.Map;
import java.util.TreeMap;

import dust.gen.DustGenException;
import dust.gen.DustGenFactory;
import dust.gen.DustGenTranslator;

public class ArkDockMind implements ArkDockDslConsts, ArkDockDsl, ArkDockBootConsts {
//	public final DslModel dslModel;
//	public final DslIdea dslIdea;
//	public final DslNative dslNative;
//	
//	public final DslText dslText;
//	public final DslGeneric dslGeneric;

	final DustGenFactory<Class<?>, Object> factDslWrap = new DustGenFactory<Class<?>, Object>(null) {
		private static final long serialVersionUID = 1L;

		protected Object createItem(Class<?> key, Object hint) {
			try {
				return key.newInstance();
			} catch (Throwable e) {
				return DustGenException.throwException(e, "Failed to instantiate token container for class", key);
			}
		};
	};

	final DustGenFactory<String, ArkDockDslBuilder> factDslBuilder = new DustGenFactory<String, ArkDockDslBuilder>(
			null) {
		private static final long serialVersionUID = 1L;

		protected ArkDockDslBuilder createItem(String key, Object hint) {
			ArkDockDslBuilder ret;

			ArkDockUnit u = factUnit.peek(key);

			if ( null == u ) {
				ret = new ArkDockDslBuilder(key);
			} else {
				ret = new ArkDockDslBuilder(u);
			}
			
			factUnit.put(key, ret);

			return ret;
		};
	};

	DustGenFactory<String, ArkDockUnit> factUnit = new DustGenFactory<String, ArkDockUnit>(null) {
		private static final long serialVersionUID = 1L;

		@Override
		protected ArkDockUnit createItem(String key, Object hint) {
			return new ArkDockUnit(key, (null == hint) ? mainUnit : (ArkDockUnit) hint);
		}
	};

	private final Map<String, ArkDockAgent<?>> agents = new TreeMap<>();

	DustGenFactory<DustEntity, ArkMemberDef> factMemberDef = new DustGenFactory<DustEntity, ArkMemberDef>(null) {
		private static final long serialVersionUID = 1L;

		@Override
		protected ArkMemberDef createItem(DustEntity key, Object hint) {
			return new ArkMemberDef((ArkDockEntity) key, (ArkDockEntity) hint);
		}
	};

	DustGenFactory<DustEntity, ArkTypeDef> factTypeDef = new DustGenFactory<DustEntity, ArkTypeDef>(null) {
		private static final long serialVersionUID = 1L;

		@Override
		protected ArkTypeDef createItem(DustEntity key, Object hint) {
			return new ArkTypeDef((ArkDockEntity) key, (ArkDockEntity) hint);
		}
	};

	final ArkDockEntity typUnit;
	final ArkDockEntity memEntityId;
	final ArkDockEntity memEntityGlobalId;
	final ArkDockEntity memEntityPrimType;
	final ArkDockEntity memEntityOwner;

	final ArkDockEntity typType;
	final ArkDockEntity typMember;
	final ArkDockEntity typTag;
	final ArkDockEntity typAgent;
	final ArkDockEntity tagColltypeOne;

	final ArkDockEntity memBinaryName;
	final ArkDockEntity memNativeCollType;
	final ArkDockEntity memNativeValueOne;

	DustGenTranslator<DustCollType, DustEntity> trCollType = new DustGenTranslator<DustCollType, DustEntity>();
	DustGenTranslator<DustValType, DustEntity> trValType = new DustGenTranslator<DustValType, DustEntity>();

	public final ArkDockUnit mainUnit;

	class BootItem {
		ArkDockDslBuilder db;

		String id;
		String type;
		String parent;
		String globalId;

		ArkDockEntity entity;

		public BootItem(ArkDockDslBuilder db, String typeId, String itemId, String parentId) {
			this.db = db;
			this.type = typeId;
			this.parent = parentId;
			this.id = (null == parent) ? itemId : parentId + TOKEN_SEP + itemId;

			this.globalId = ArkDockUtils.buildGlobalId(db.unitName, type, id);

			entity = new ArkDockEntity(db);
			db.entities.put(globalId, entity);
		}

		void updateEntity(Map<String, BootItem> boot) {
			initEntityBoot(entity, globalId, boot.get(type).entity, id,
					(null == parent) ? null : boot.get(parent).entity);
		}
	}

	class Bootloader {
		Map<String, BootItem> boot = new TreeMap<String, BootItem>();
//		Map<ArkDockDslBuilder> dsls = new TreeSet<ArkDockDslBuilder>();

		ArkDockEntity createBootEntity(ArkDockDslBuilder db, String typeId, String itemId, String parentId) {
			BootItem be = new BootItem(db, typeId, itemId, parentId);
			boot.put(itemId, be);

//			if ( dsls.add(db) ) {
//				BootItem b = new BootItem(db, TYPENAME_UNIT,  db.unitName,  null);
//				boot.put("", b);
//			}

			return be.entity;
		}

		ArkDockEntity createBootEntity(ArkDockDslBuilder db, String typeId, String itemId) {
			return createBootEntity(db, typeId, itemId, null);
		}

		void updateEntities() {
			for (BootItem bi : boot.values()) {
				bi.updateEntity(boot);
			}
		}
	}

	public ArkDockMind(String mainUnitName) {
		ArkDock.setMind(this);

		Bootloader bl = new Bootloader();

		ArkDockDslBuilder dbModel = factDslBuilder.get(UNITNAME_MODEL);
		typUnit = bl.createBootEntity(dbModel, TYPENAME_TYPE, TYPENAME_UNIT);
		bl.createBootEntity(dbModel, TYPENAME_TYPE, TYPENAME_ENTITY);
		memEntityId = bl.createBootEntity(dbModel, TYPENAME_MEMBER, MEMBERNAME_ENTITY_ID, TYPENAME_ENTITY);
		memEntityGlobalId = bl.createBootEntity(dbModel, TYPENAME_MEMBER, MEMBERNAME_ENTITY_GLOBALID, TYPENAME_ENTITY);
		memEntityPrimType = bl.createBootEntity(dbModel, TYPENAME_MEMBER, MEMBERNAME_ENTITY_PRIMARYTYPE, TYPENAME_ENTITY);
		memEntityOwner = bl.createBootEntity(dbModel, TYPENAME_MEMBER, MEMBERNAME_ENTITY_OWNER, TYPENAME_ENTITY);

		typUnit.data.put(memEntityId, TYPENAME_UNIT);
		dbModel.eUnit = bl.createBootEntity(dbModel, TYPENAME_UNIT, UNITNAME_MODEL);

		ArkDockDslBuilder dbIdea = factDslBuilder.get(UNITNAME_IDEA);
		typType = bl.createBootEntity(dbIdea, TYPENAME_TYPE, TYPENAME_TYPE);
		typAgent = bl.createBootEntity(dbIdea, TYPENAME_TYPE, TYPENAME_AGENT);
		typMember = bl.createBootEntity(dbIdea, TYPENAME_TYPE, TYPENAME_MEMBER);
		typTag = bl.createBootEntity(dbIdea, TYPENAME_TYPE, TYPENAME_TAG);

		tagColltypeOne = bl.createBootEntity(dbIdea, TYPENAME_TAG, TAGNAME_COLLTYPE_ONE, TAGNAME_COLLTYPE);

		ArkDockDslBuilder dbNative = factDslBuilder.get(UNITNAME_NATIVE);
		bl.createBootEntity(dbNative, TYPENAME_TYPE, TYPENAME_NATIVE);
		memNativeCollType = bl.createBootEntity(dbNative, TYPENAME_MEMBER, MEMBERNAME_NATIVE_COLLTYPE, TYPENAME_NATIVE);
		memNativeValueOne = bl.createBootEntity(dbNative, TYPENAME_MEMBER, MEMBERNAME_NATIVE_VALUEONE, TYPENAME_NATIVE);
		bl.createBootEntity(dbNative, TYPENAME_TYPE, TYPENAME_BINARY);
		memBinaryName = bl.createBootEntity(dbNative, TYPENAME_MEMBER, MEMBERNAME_BINARY_NAME, TYPENAME_BINARY);

		bl.updateEntities();

		getDsl(DslModel.class);
		DslIdea dslIdea = getDsl(DslIdea.class);

		dbModel.eUnit = dbModel.getEntity(typUnit, UNITNAME_MODEL, true);

		trCollType.add(DustCollType.ONE, dslIdea.tagColltypeOne);
		trCollType.add(DustCollType.ARR, dslIdea.tagColltypeArr);
		trCollType.add(DustCollType.SET, dslIdea.tagColltypeSet);
		trCollType.add(DustCollType.MAP, dslIdea.tagColltypeMap);

		trValType.add(DustValType.INT, dslIdea.tagValtypeInt);
		trValType.add(DustValType.REAL, dslIdea.tagValtypeReal);
		trValType.add(DustValType.REF, dslIdea.tagValtypeRef);
		trValType.add(DustValType.RAW, dslIdea.tagValtypeRaw);

		getDsl(DslNarrative.class);
		getDsl(DslDialog.class);
		getDsl(DslGeneric.class);
		getDsl(DslText.class);

		mainUnit = factUnit.get(mainUnitName);
	}

	@SuppressWarnings("unchecked")
	public <DslType> DslType getDsl(Class<DslType> dslClass) {
		return (DslType) factDslWrap.get(dslClass);
	}
	
	protected ArkDockDslBuilder getDslBuilder(String unitName) {
		return factDslBuilder.get(unitName);
	}

	public ArkMemberDef getMemberDef(DustEntity member, Object value, Object hint) {
		ArkMemberDef amd = factMemberDef.get(member);

		if ( (null == amd.ct) && (null != hint) ) {
			amd.ct = ArkDockUtils.getCollTypeForHint(hint);
		}

		if ( (null == amd.vt) && (null != value) ) {
			amd.vt = ArkDockUtils.getValTypeForValue(value);
		}

		return amd;
	}

	void initEntity(DustEntity entity, String unitId, DustEntity type, String itemId, DustEntity owner) {
		String globalId = ArkDockUtils.buildGlobalId(unitId, ArkDock.getId(type), itemId);
		initEntityBoot(entity, globalId, type, itemId, owner);
	}

	void initEntityBoot(DustEntity entity, String globalId, DustEntity type, String itemId, DustEntity owner) {
		ArkDockEntity e = (ArkDockEntity) entity;

		e.data.put(memEntityId, itemId);
		e.data.put(memEntityGlobalId, globalId);
		e.data.put(memEntityPrimType, type);
		if ( null != owner ) {
			e.data.put(memEntityOwner, owner);
		}
	}

	public ArkDockUnit getMainUnit() {
		return mainUnit;
	}

	public final ArkDockUnit getUnit(String unitName, ArkDockUnit parent) {
		return factUnit.get(unitName, parent);
	}

	public final DustEntity getEntity(DustEntity eType, String id, boolean createIfMissing) {
		return mainUnit.getEntity(eType, id, createIfMissing);
	}

	public final DustEntity getEntity(String unitName, DustEntity eType, String id, boolean createIfMissing) {
		return factUnit.get(unitName).getEntity(eType, id, createIfMissing);
	}

	public final DustEntity getAgent(String agentName) {
		return getEntity(typAgent, agentName, false);
	}

	protected ArkDockAgent<?> createAgent(String agentName) throws Exception {
		DustEntity eAgent = getAgent(agentName);

		String cName = mainUnit.getMember(eAgent, memBinaryName, null, null);
		Class<?> c = Class.forName(cName);

		ArkDockAgent<?> agent = (ArkDockAgent<?>) c.newInstance();

		return agent;
	}

	public final DustResultType initAgent(String agentName) throws Exception {
		ArkDockAgent<DustEntityContext> agent = (ArkDockAgent<DustEntityContext>) createAgent(agentName);

		DustEntity eAgent = getAgent(agentName);

		mainUnit.setMember(eAgent, memNativeCollType, tagColltypeOne, null);
		mainUnit.setMember(eAgent, memNativeValueOne, agent, null);

		DustResultType res = sendAgent(agent, DustAgentAction.INIT, eAgent, null, null, null);

		agents.put(agentName, agent);

		return res;
	}

	public final DustResultType sendAgent(String agentName, DustAgentAction cmd) throws Exception {
		return sendAgent(agentName, cmd, null, null, null, null);
	}

	public final DustResultType sendAgent(String agentName, DustAgentAction cmd, DustEntity e, DustEntity m, Object val,
			Object key) throws Exception {
		ArkDockAgent<DustEntityContext> agent = (ArkDockAgent<DustEntityContext>) agents.get(agentName);
		return sendAgent(agent, cmd, e, m, val, key);
	}

	public final DustResultType sendAgent(ArkDockAgent<DustEntityContext> agent, DustAgentAction cmd, DustEntity e,
			DustEntity m, Object val, Object key) throws Exception {
		DustEntityContext ctx = new DustEntityContext();

		ctx.entity = e;
		ctx.member = m;
		ctx.value = val;
		ctx.key = key;

		try {
			agent.setActionCtx(ctx);
			return agent.agentAction(cmd);
		} finally {
			agent.setActionCtx(null);
		}
	}
}
