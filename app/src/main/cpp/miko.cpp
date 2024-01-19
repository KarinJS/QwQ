#include "lsp/lsposed.h"

static HookFunType hook_function = nullptr;

void on_library_loaded(const char *name, void *handle) {

}

extern "C" [[gnu::visibility("default")]] [[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    hook_function = entries->hook_func;
    LOGI("[QwQ] LSPosed NativeModule Init: %p", hook_function);

    return on_library_loaded;
}