package ark.dock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dust.gen.DustGenConsts.DustEntity;
import dust.gen.DustGenCounter;
import dust.gen.DustGenException;
import dust.gen.DustGenUtils;

public class ArkDockUnit implements ArkDockConsts, Iterable<DustEntity> {

	protected final ArkDockMind mind;
	private final ArkDockUnit parent;
	protected final Map<String, ArkDockEntity> entities = new HashMap<>();

	final String unitName;
	ArkDockEntity eUnit;

	ArkDockUnit(ArkDockUnit src) {
		this.unitName = src.unitName;
		this.parent = src.parent;
		this.mind = ArkDock.getMind();
		eUnit = src.eUnit;
	}

	public ArkDockUnit(String unitName_, ArkDockUnit parent_) {
		this.unitName = unitName_;
		this.parent = parent_;
		this.mind = ArkDock.getMind();
		eUnit = (null == mind.typUnit) ? null : getEntity(mind.typUnit, unitName, true);
	}

	public DustEntity getUnit() {
		return eUnit;
	}

	public ArkDockUnit getParent() {
		return parent;
	}

	public ArkDockEntity getNewEntity(DustEntity type) {
		return getEntity(type, null, true);
	}

	public ArkDockEntity getEntity(DustEntity type, String itemId, boolean createIfMissing) {
		if ( null == itemId ) {
			itemId = ArkDockEntity.getNextUniqueId();
		} 
		
		String globalId = ArkDockUtils.buildGlobalId(unitName, (String) ((ArkDockEntity) type).data.get(mind.memEntityId), itemId);

		ArkDockEntity e = entities.get(globalId);

		if ( createIfMissing && (null == e) ) {
			e = new ArkDockEntity(this);
			mind.initEntityBoot(e, globalId, type, itemId, null);
			entities.put(globalId, e);
		}

		return e;
	}

	public ArkDockEntity getEntity(String globalId) {
		ArkDockEntity e = entities.get(globalId);
		if ( (null == e) && (null != parent) ) {
			e = parent.getEntity(globalId);
		}
		return e;
	}

	public int getCount(DustEntity e, DustEntity member) {
		return (e instanceof ArkDockEntity) ? ((ArkDockEntity) e).getCount(member) : 0;
	}

//	public String getId(DustEntity e) {
//		return (e instanceof ArkDockEntity) ? ((ArkDockEntity) e).id : null;
//	}
//
//	public String getGlobalId(DustEntity e) {
//		return (e instanceof ArkDockEntity) ? ((ArkDockEntity) e).globalId : null;
//	}

	@SuppressWarnings("unchecked")
	public <RetType> RetType accessMember(DustDialogCmd cmd, DustEntity e, DustEntity member, Object value,
			Object hint) {
		Object ret = (cmd == DustDialogCmd.GET) ? value : false;

		if ( null != e ) {
			if ( null == member ) {
				switch ( cmd ) {
				case ADD:
					break;
				case CHK:
					break;
				case DEL:
					Object o = entities.remove(ArkDock.getGlobalId(e));
					o.toString();
					break;
				case GET:
					break;
				case SET:
					break;
				default:
					break;

				}
			} else {
				ret = ((ArkDockEntity) e).accessMember(cmd, member, value, hint);
			}
		}

		return (RetType) ret;
	}

	public <RetType> RetType accessMember(DustEntity entity, DustEntityDelta delta) {
		return accessMember(delta.cmd, entity, delta.member, delta.value, delta.key);
	}

	private DustResultType doProcess(ArkDockAgent<? extends DustEntityContext> visitor, Object val, Object key)
			throws Exception {
		DustEntityContext ctx = visitor.getActionCtx();

		ctx.key = key;
		ctx.value = val;

		DustResultType ret = visitor.agentAction(DustAgentAction.PROCESS);

		if ( DustGenUtils.isReadOn(ret) && (ctx.valType == DustValType.REF) ) {
			DustEntityContext save = new DustEntityContext(ctx);
			ctx.reset();
			ret = doVisitEntity(visitor, (ArkDockEntity) val);
			ctx.load(save);
		}

		return ret;
	}

	@SuppressWarnings("rawtypes")
	private DustResultType doVisitMember(ArkDockAgent<? extends DustEntityContext> visitor, DustEntity member)
			throws Exception {
		DustResultType ret = null;
		DustEntityContext ctx = visitor.getActionCtx();
		ArkDockEntity entity = (ArkDockEntity) ctx.entity;
		ctx.member = member;

		DustMemberDef md = mind.getMemberDef(member, ctx.value, ctx.key);
		ctx.valType = md.getValType();
		ctx.collType = md.getCollType();

		ctx.block = EntityBlock.Member;
		ret = visitor.agentAction(DustAgentAction.BEGIN);

		if ( DustGenUtils.isReadOn(ret) ) {
			try {
				Object val = entity.data.get(member);

				DustCollType actCollType;

				if ( (null == ctx.key) && (null != (actCollType = ArkDockUtils.getCollTypeForData(val))) ) {
					int idx = 0;
					switch ( actCollType ) {
					case ARR:
					case SET:
						for (Object o : (Collection) val) {
							ret = doProcess(visitor, o, idx++);
							if ( DustGenUtils.isReject(ret) ) {
								break;
							}
						}
						break;
					case MAP:
						for (Object o : ((Map) val).entrySet()) {
							Map.Entry mm = (Map.Entry) o;
							ret = doProcess(visitor, mm.getValue(), mm.getKey());
							if ( DustGenUtils.isReject(ret) ) {
								break;
							}
						}
						break;
					default:
						break;
					}
					ctx.key = null;
				} else {
					ret = doProcess(visitor, ArkDockUtils.resolveValue(md, val, ctx.key), ctx.key);
				}
			} finally {
				ctx.entity = entity;
				ctx.member = md.getDefEntity();
				ctx.valType = md.getValType();
				ctx.collType = md.getCollType();
				ctx.key = ctx.value = null;

				ctx.block = EntityBlock.Member;
				visitor.agentAction(DustAgentAction.END);
			}
		}

		return ret;
	}

	DustResultType doVisitEntity(ArkDockAgent<? extends DustEntityContext> visitor, ArkDockEntity entity)
			throws Exception {
		DustResultType ret = null;
		DustEntityContext ctx = visitor.getActionCtx();

		ctx.entity = entity;
		ctx.block = EntityBlock.Entity;
		ret = visitor.agentAction(DustAgentAction.BEGIN);

		Object eKey = ctx.entityId;

		if ( DustGenUtils.isReadOn(ret) ) {
			try {
				if ( null == ctx.member ) {
					for (DustEntity de : entity.data.keySet()) {
						ret = doVisitMember(visitor, de);
						if ( DustGenUtils.isReject(ret) ) {
							break;
						}
					}
					return ret;
				} else {
					ret = doVisitMember(visitor, ctx.member);
				}

			} finally {
				ctx.entity = entity;
				ctx.entityId = eKey;
				ctx.member = null;
				ctx.valType = null;
				ctx.collType = null;

				ctx.block = EntityBlock.Entity;
				visitor.agentAction(DustAgentAction.END);
			}
		}

		return ret;
	}

	public DustResultType visit(ArkDockAgent<? extends DustEntityContext> visitor) throws Exception {
		return visit(visitor, null, null, null, null);
	}

	public DustResultType visit(ArkDockAgent<? extends DustEntityContext> visitor, DustEntity e, DustEntity member,
			Object key) throws Exception {
		return visit(visitor, e, null, member, key);
	}

	public DustResultType visit(ArkDockAgent<? extends DustEntityContext> visitor, Iterable<DustEntity> eIt,
			DustEntity member, Object key) throws Exception {
		return visit(visitor, null, eIt, member, key);
	}

	private DustResultType visit(ArkDockAgent<? extends DustEntityContext> visitor, DustEntity e,
			Iterable<DustEntity> eIt, DustEntity member, Object key) throws Exception {
		DustResultType ret = null;

		DustEntityContext ctx = visitor.getActionCtx();
		ctx.entity = e;
		ctx.member = member;
		ctx.key = key;

		visitor.agentAction(DustAgentAction.INIT);

		try {
			if ( null == e ) {
				Iterable<? extends DustEntity> root = (null == eIt) ? entities.values() : eIt;
				for (DustEntity me : root) {
					ret = doVisitEntity(visitor, (ArkDockEntity) me);
					if ( DustGenUtils.isReject(ret) ) {
						break;
					}
				}
			} else {
				ret = doVisitEntity(visitor, (ArkDockEntity) e);
			}
		} finally {
			ctx.reset();
			ctx.entity = e;
			ctx.member = member;
			ctx.key = key;
			visitor.agentAction(DustAgentAction.RELEASE);
		}

		return ret;
	}

	public boolean setMember(DustEntity e, DustEntity member, Object value, Object hint) {
		return (boolean) accessMember(DustDialogCmd.SET, e, member, value, hint);
	}

	public <RetType> RetType getMember(DustEntity e, DustEntity member, RetType defValue, Object hint) {
		return accessMember(DustDialogCmd.GET, e, member, defValue, hint);
	}

	public Iterator<DustEntity> getContent(DustEntity e, DustEntity member) {
		return ((ArkDockEntity) e).data.keySet().iterator();
	}

	class ContentReader implements Iterator<DustEntity> {
		Iterator<ArkDockEntity> mi;

		public ContentReader() {
			mi = entities.values().iterator();
		}

		@Override
		public boolean hasNext() {
			return mi.hasNext();
		}

		@Override
		public DustEntity next() {
			return mi.next();
		}

		@Override
		public void remove() {
			DustGenException.throwException(null, "Should not remove Entity in this way! Use the API.");
		}
	}

	@Override
	public Iterator<DustEntity> iterator() {
		return new ContentReader();
	}

	@Override
	public String toString() {
		DustGenCounter counter = new DustGenCounter(true);

		for (ArkDockEntity me : entities.values()) {
			counter.add(me.data.get(mind.memEntityPrimaryType).toString());
		}

		return counter.toString();
	}
}