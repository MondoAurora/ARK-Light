package ark.dock;

public interface ArkDockDslMind extends ArkDockConsts, ArkDockBootConsts {

	public class DslModel {
		public final DustEntity unit;

		public final DustEntity typUnit;
		public final DustEntity typEntity;

		public final DustEntity memEntityUnit;
		public final DustEntity memEntityId;
		public final DustEntity memEntityGlobalId;
		public final DustEntity memEntityPrimaryType;
		public final DustEntity memEntityOwner;
		public final DustEntity memEntityTags;

		public DslModel() {
			ArkDockDslBuilder meta = ArkDock.getDslBuilder(UNITNAME_MODEL);
			unit = meta.getUnit();

			typUnit = meta.getType(TYPENAME_UNIT);
			typEntity = meta.getType(TYPENAME_ENTITY);

			memEntityUnit = meta.defineMember(typEntity, "Unit", DustValType.REF, DustCollType.ONE);
			memEntityId = meta.defineMember(typEntity, MEMBERNAME_ENTITY_ID, DustValType.RAW, DustCollType.ONE);
			memEntityGlobalId = meta.defineMember(typEntity, MEMBERNAME_ENTITY_GLOBALID, DustValType.RAW,
					DustCollType.ONE);
			memEntityPrimaryType = meta.defineMember(typEntity, MEMBERNAME_ENTITY_PRIMARYTYPE, DustValType.REF,
					DustCollType.ONE);
			memEntityOwner = meta.defineMember(typEntity, MEMBERNAME_ENTITY_OWNER, DustValType.REF, DustCollType.ONE);
			memEntityTags = meta.defineMember(typEntity, "Tags", DustValType.REF, DustCollType.SET);
		}
	}

	public class DslIdea {
		public final DustEntity unit;

		public final DustEntity typType;

		public final DustEntity typAgent;
		public final DustEntity memAgentUpdates;
		public final DustEntity memAgentWrappedObj;

		public final DustEntity typMember;
		public final DustEntity memMemberOptions;

		public final DustEntity typTag;

		public final DustEntity tagBool;
		public final DustEntity tagBoolTrue;
		public final DustEntity tagBoolFalse;

		public final DustEntity tagValtype;
		public final DustEntity tagValtypeInt;
		public final DustEntity tagValtypeReal;
		public final DustEntity tagValtypeRef;
		public final DustEntity tagValtypeRaw;

		public final DustEntity tagColltype;
		public final DustEntity tagColltypeOne;
		public final DustEntity tagColltypeArr;
		public final DustEntity tagColltypeSet;
		public final DustEntity tagColltypeMap;

		public DslIdea() {
			ArkDockDslBuilder meta = ArkDock.getDslBuilder(UNITNAME_IDEA);

			unit = meta.getUnit();

			typType = meta.getType(TYPENAME_TYPE);
			typAgent = meta.getType(TYPENAME_AGENT);
			typMember = meta.getType(TYPENAME_MEMBER);
			typTag = meta.getType(TYPENAME_TAG);

			memMemberOptions = meta.defineMember(typMember, "Options", DustValType.REF, DustCollType.SET);

			memAgentUpdates = meta.defineMember(typAgent, "Updates", DustValType.REF, DustCollType.SET);
			memAgentWrappedObj = meta.defineMember(typAgent, "WrappedObj", DustValType.RAW, DustCollType.ONE);

			tagBool = meta.defineTag("Boolean", null);
			tagBoolFalse = meta.defineTag("False", tagBool);
			tagBoolTrue = meta.defineTag("True", tagBool);

			tagValtype = meta.defineTag("ValType", null);
			tagValtypeInt = meta.defineTag("Int", tagValtype);
			tagValtypeReal = meta.defineTag("Real", tagValtype);
			tagValtypeRef = meta.defineTag("Ref", tagValtype);
			tagValtypeRaw = meta.defineTag("Raw", tagValtype);

			tagColltype = meta.defineTag("CollType", null);
			tagColltypeOne = meta.defineTag("One", tagColltype);
			tagColltypeArr = meta.defineTag("Arr", tagColltype);
			tagColltypeSet = meta.defineTag("Set", tagColltype);
			tagColltypeMap = meta.defineTag("Map", tagColltype);
		}
	}

	public class DslNarrative {
		public final DustEntity unit;

		public DslNarrative() {
			ArkDockDslBuilder meta = ArkDock.getDslBuilder("Narrative");
			unit = meta.getUnit();
		}
	}

	public class DslDialog {
		public final DustEntity unit;

		public final DustEntity typAction;

		public final DustEntity memActionRespRaw;

		public DslDialog() {
			ArkDockDslBuilder meta = ArkDock.getDslBuilder("Dialog");
			unit = meta.getUnit();

			typAction = meta.getType("Action");

			memActionRespRaw = meta.defineMember(typAction, "RespRaw", DustValType.RAW, DustCollType.ONE);
		}
	}

}
