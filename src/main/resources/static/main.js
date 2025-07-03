function logout() {
    if (confirm('Are you sure you want to logout?')) {
        fetch('/logout', {
            method: 'POST',
            credentials: 'include'
        }).then(() => {
            window.location.href = '/login?logout';
        }).catch(error => {
            console.error('Logout failed:', error);
            showToast("Logout failed. Please try again.", "error");
        });
    }
}

// Toast notification system
function showToast(message, type = "info", duration = 5000) {
    const toastContainer = document.getElementById('toast-container');

    // Clear any existing toasts
    toastContainer.innerHTML = '';

    const toast = document.createElement('div');

    // Generate a unique ID for this toast
    const toastId = 'toast-' + Date.now();
    toast.id = toastId;
    toast.className = `popup-message ${type}`;

    // Create message content in a separate element
    const messageElement = document.createElement('span');
    messageElement.className = 'popup-message-content';
    messageElement.innerHTML = message;

    // Create close button
    const closeButton = document.createElement('span');
    closeButton.className = 'popup-message-close';
    closeButton.innerHTML = '&times;';
    closeButton.onclick = () => removeToast(toastId);

    // Add message and close button to toast
    toast.appendChild(messageElement);
    toast.appendChild(closeButton);

    // Add to container
    toastContainer.appendChild(toast);

    // Show the toast
    setTimeout(() => {
        toast.style.display = 'block';
    }, 10);

    // Auto-remove after duration
    if (duration > 0) {
        setTimeout(() => removeToast(toastId), duration);
    }

    return toastId;
}

function removeToast(toastId) {
    const toast = document.getElementById(toastId);
    if (toast) {
        // Add fade-out animation before removing
        toast.style.animation = 'fadeOut 0.3s ease-out forwards';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }
}

document.addEventListener('DOMContentLoaded', function () {
    // Form submission handling
    function setupFormHandlers() {
        // Settings form handler
        const settingsForm = document.getElementById('settings-form');
        if (settingsForm) {
            settingsForm.addEventListener('submit', function (e) {
                e.preventDefault(); // Prevent default form submission
                handleFormSubmit(settingsForm, '/api/settings/update', 'Settings updated successfully.');
            });
        }

        // Generic form submission handler for AJAX forms
        function handleFormSubmit(form, endpoint, successMessage) {
            const formData = new FormData(form);

            // Show loading indicator
            showToast('Processing...', 'info');

            fetch(endpoint, {
                method: 'POST',
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    return response.text();
                })
                .then( data => {
                    showToast(data || successMessage, "success");
                })
                .catch(error => {
                    console.error(`Error submitting to ${endpoint}:`, error);
                    showToast("An error occurred. Please try again.", "error");
                });
        }
    }

    // Initialize all form handlers
    setupFormHandlers();

    const initRunTestPage = async () => {
        const addActionBtn = document.getElementById('add-action-btn');
        const actionsContainer = document.getElementById('actions-container');
        const jsonPreview = document.getElementById('json-preview');
        const form = document.getElementById('test-builder-form');
        const testDataSelect = document.getElementById('test-data');

        if (!form || !addActionBtn || !actionsContainer || !jsonPreview) {
            showToast('Some required elements for the Test Builder are missing. Please refresh the page or contact support if the issue persists.', "error");
            return;
        }

        let actionDefinitions = {};
        let locatorTypes = [];
        let selectByTypes = [];

        // Fetch all required data including test data sets
        try {
            const [actionsResponse, locatorTypesResponse, selectByTypesResponse, testDataResponse] = await Promise.all([
                fetch('/api/actions'),
                fetch('/api/actions/locators'),
                fetch('/api/actions/select-by'),
                fetch('/api/testdata')
            ]);
            actionDefinitions = await actionsResponse.json();
            locatorTypes = await locatorTypesResponse.json();
            selectByTypes = await selectByTypesResponse.json();

            // Populate test data dropdown
            const testDataSets = await testDataResponse.json();
            if (testDataSelect) {
                testDataSelect.innerHTML = '<option value="">None - No test data</option>';
                testDataSets.forEach(dataSet => {
                    const option = document.createElement('option');
                    option.value = dataSet.id;
                    option.textContent = `${dataSet.name}${dataSet.description ? ' - ' + dataSet.description : ''}`;
                    testDataSelect.appendChild(option);
                });

                // Add change handler to show available variables when test data is selected
                testDataSelect.addEventListener('change', function () {
                    showTestDataPreview(this.value, testDataSets);
                });
            }
        } catch (error) {
            showToast('Failed to load Test Builder data. Please try again or contact support if the issue persists.', "error");
            return;
        }

        // Function to show test data variables preview
        const showTestDataPreview = (selectedId, testDataSets) => {
            const previewContainer = document.getElementById('test-data-variables-preview');
            if (!previewContainer) {
                // Create preview container if it doesn't exist
                const container = document.createElement('div');
                container.id = 'test-data-variables-preview';
                container.className = 'test-data-preview';
                container.innerHTML = `
                        <h4><i class="fas fa-eye"></i> Available Variables</h4>
                        <div id="variables-list"></div>
                    `;
                testDataSelect.parentNode.appendChild(container);
            }

            const variablesList = document.getElementById('variables-list');
            if (!selectedId) {
                document.getElementById('test-data-variables-preview').style.display = 'none';
                return;
            }

            const selectedDataSet = testDataSets.find(ds => ds.id == selectedId);
            if (selectedDataSet) {
                try {
                    const data = JSON.parse(selectedDataSet.dataJson);
                    const variables = extractVariablesFromData(data);

                    document.getElementById('test-data-variables-preview').style.display = 'block';
                    variablesList.innerHTML = `
                            <p class="text-muted">Use these variables in your action parameters:</p>
                            <div class="variables-grid">
                                ${variables.map(variable => {
                        const actualValue = getActualValue(variable, data);
                        return `
                                        <div class="variable-chip" onclick="copyToClipboard('${variable}')" title="Click to copy: ${variable}">
                                            <div class="variable-display">
                                                <span class="variable-name">${variable.replace(/^\$\{|\}$/g, '')}</span>
                                                <span class="variable-actual-value">${actualValue}</span>
                                            </div>
                                            <i class="fas fa-copy"></i>
                                        </div>
                                    `;
                    }).join('')}
                            </div>
                            <p class="text-info"><i class="fas fa-info-circle"></i> Click on any variable to copy it to clipboard</p>
                        `;
                } catch (error) {
                    variablesList.innerHTML = '<p class="text-danger">Error parsing test data JSON</p>';
                }
            }
        };

        // Function to extract variables from test data
        const extractVariablesFromData = (data, prefix = '') => {
            const variables = [];
            for (const key in data) {
                const path = prefix ? `${prefix}.${key}` : key;
                if (typeof data[key] === 'object' && data[key] !== null) {
                    variables.push(...extractVariablesFromData(data[key], path));
                } else {
                    variables.push(`\${${path}}`);
                }
            }
            return variables;
        };

        // Function to get the actual value for a variable
        const getActualValue = (variable, data) => {
            // Remove ${ and } from variable name
            const path = variable.replace(/^\$\{|\}$/g, '');
            const parts = path.split('.');
            let current = data;

            for (const part of parts) {
                if (current && typeof current === 'object' && current.hasOwnProperty(part)) {
                    current = current[part];
                } else {
                    return 'undefined';
                }
            }

            // Truncate long values for display
            const value = String(current);
            return value.length > 30 ? value.substring(0, 27) + '...' : value;
        };

        // Copy to clipboard function
        window.copyToClipboard = function (text) {
            navigator.clipboard.writeText(text).then(() => {
                showToast(`Copied "${text}" to clipboard!`, 'success', 2000);
            }).catch(() => {
                showToast('Failed to copy to clipboard', 'error');
            });
        };

        // Create input field based on parameter type
        const createInputField = (param, key) => {
            switch (param) {
                case 'locator.type':
                case 'sourceLocator.type':
                case 'targetLocator.type':
                    return `<select class="form-control" data-param="${param}">
                            ${locatorTypes.map(type =>
                        `<option value="${type}">${type.charAt(0).toUpperCase() + type.slice(1)}</option>`
                    ).join('')}
                        </select>`;
                case 'selectBy':
                    return `<select class="form-control" data-param="${param}">
                            ${selectByTypes.map(type =>
                        `<option value="${type}">${type.charAt(0).toUpperCase() + type.slice(1)}</option>`
                    ).join('')}
                        </select>`;
                case 'seconds':
                    return `<input type="number"
                               class="form-control"
                               data-param="${param}"
                               min="0"
                               value="1">`;
                default:
                    return `<input type="text"
                               class="form-control"
                               data-param="${param}"
                               placeholder="Enter ${key}">`;
            }
        };

        // Create parameter group (label + input)
        const createParamGroup = (param) => {
            const [key, subKey] = param.split('.');
            const label = subKey ? `${key} (${subKey})` : key;

            return `
                    <div class="form-group mt-2">
                        <label>${label}</label>
                        <div class="input-group">
                            ${createInputField(param, label)}
                        </div>
                    </div>`;
        };

        // Create HTML for action parameters
        const createActionParams = (actionType) => {
            if (!actionDefinitions[actionType]) {
                return '<p class="text-muted">Select an action type to see its parameters.</p>';
            }

            return actionDefinitions[actionType]
                .map(param => createParamGroup(param))
                .join('');
        };

        // Create a new action block
        const createActionBlock = () => {
            const block = document.createElement('div');
            block.className = 'action-block';
            block.innerHTML = `
                    <div class="action-block-header">
                        <h5>Action</h5>
                        <button type="button" class="btn btn-danger btn-sm remove-action-btn">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>
                    <div class="form-group">
                        <label>Action Type</label>
                        <select class="form-control action-type-selector">
                            <option value="">-- Select Action --</option>
                            ${Object.keys(actionDefinitions)
                .map(action => `<option value="${action}">${action.charAt(0).toUpperCase() + action.slice(1)}</option>`)
                .join('')}
                        </select>
                    </div>
                    <div class="action-params mt-2"></div>`;
            actionsContainer.appendChild(block);
        };

        // Update action parameters when action type changes
        actionsContainer.addEventListener('change', e => {
            if (e.target.matches('.action-type-selector')) {
                const actionType = e.target.value;
                const paramsDiv = e.target.closest('.action-block').querySelector('.action-params');
                paramsDiv.innerHTML = createActionParams(actionType);
                updatePreview();
            }
        });

        const updatePreview = () => {
            const actions = Array.from(document.querySelectorAll('.action-block')).map(block => {
                const type = block.querySelector('.action-type-selector')?.value;
                if (!type) return null;
                const action = {action: type};
                const params = {};

                block.querySelectorAll('[data-param]').forEach(input => {
                    const key = input.dataset.param;
                    const value = input.type === 'checkbox' ? input.checked : input.value;
                    const keys = key.split('.');
                    let current = params;
                    keys.forEach((k, i) => {
                        if (i === keys.length - 1) current[k] = value;
                        else current = current[k] = current[k] || {};
                    });
                });

                return {...action, ...params};
            }).filter(Boolean);

            jsonPreview.textContent = JSON.stringify(actions, null, 2);
        };

        actionsContainer.addEventListener('input', updatePreview);

        actionsContainer.addEventListener('click', e => {
            if (e.target.closest('.remove-action-btn')) {
                e.target.closest('.action-block').remove();
                updatePreview();
            }
        });

        addActionBtn.addEventListener('click', createActionBlock);

        form.addEventListener('submit', (e) => {
            e.preventDefault();

            let actions;
            try {
                actions = JSON.parse(jsonPreview.textContent);
                if (!Array.isArray(actions) || actions.length === 0) {
                    showToast('Add at least one action.', "error");
                    return;
                }
            } catch {
                showToast('Invalid JSON.', "error");
                return;
            }

            const builtTest = {
                browser: form.browser.value,
                stopOnFailure: form.stopOnFailure.value === 'true',
                testDataId: form['test-data'].value ? parseInt(form['test-data'].value) : null,
                actions
            };

            const builtTests = JSON.parse(sessionStorage.getItem('builtTests') || '[]');
            builtTests.push(builtTest);
            sessionStorage.setItem('builtTests', JSON.stringify(builtTests));

            showToast('Test built successfully! Navigate to the Test Runner tab to run it.', 'success');
        });

        const pollForResults = (testRunId) => {
            const resultsOutput = document.getElementById('results-output');
            const interval = setInterval(async () => {
                try {
                    const res = await fetch(`/api/tests/results/${testRunId}`);
                    const data = await res.json();
                    if (data.status === 'finished') {
                        clearInterval(interval);
                        resultsOutput.textContent = JSON.stringify(data.results, null, 2);
                    } else if (data.status === 'not_found') {
                        clearInterval(interval);
                        resultsOutput.textContent = 'Results not found.';
                    }
                } catch (err) {
                    clearInterval(interval);
                    resultsOutput.textContent = 'Polling error.';
                }
            }, 2000);
        };

        if (!actionsContainer.querySelector('.action-block')) createActionBlock();
    };
    let runTestPageInitialized = false;

    const observer = new MutationObserver(() => {
        if (!runTestPageInitialized && document.querySelector('#test-builder-form')) {
            runTestPageInitialized = true;
            requestIdleCallback(initRunTestPage);
        }
    });

    observer.observe(document.body, {childList: true, subtree: true});

// Also run once on initial load if content is already there
    if (document.querySelector('#test-builder-form')) {
        runTestPageInitialized = true;
        requestIdleCallback(initRunTestPage);
    }

    const initTestRunnerPage = () => {
        const container = document.getElementById('test-card-container');
        const noTestsMessage = document.getElementById('no-tests-message');
        if (!container || !noTestsMessage) return;

        let builtTests = JSON.parse(sessionStorage.getItem('builtTests') || '[]');

        if (builtTests.length === 0) {
            noTestsMessage.style.display = 'block';
            return;
        }

        const renderTestCards = () => {
            container.innerHTML = '';
            builtTests.forEach((test, index) => {
                // Get status from test object (default to 'pending' if not set)
                const status = test.status || 'pending';
                // Generate a unique test ID if not already present
                if (!test.id) {
                    test.id = `T${Date.now().toString().slice(-6)}-${index}`;
                    // Update the stored test with the new ID
                    sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                }

                const card = document.createElement('div');
                card.className = 'test-card';
                card.dataset.testId = test.id;

                // Generate a browser icon based on the test's browser
                const browserIcon = test.browser === 'chrome' ? 'fa-chrome' :
                    test.browser === 'firefox' ? 'fa-firefox' :
                        test.browser === 'edge' ? 'fa-edge' : 'fa-globe';

                card.innerHTML = `
                        <div class="test-status-badge status-${status}">
                            ${status === 'pass' ? '<i class="fas fa-check-circle"></i> PASS' :
                    status === 'fail' ? '<i class="fas fa-times-circle"></i> FAIL' :
                        status === 'running' ? '<i class="fas fa-spinner fa-spin"></i> RUNNING' :
                            status === 'cancelled' ? '<i class="fas fa-ban"></i> CANCELLED' :
                                '<i class="fas fa-clock"></i> PENDING'}
                        </div>
                        <div class="test-card-content">
                            <div class="test-card-main-info">
                                <div class="test-card-browser">
                                    <i class="fab ${browserIcon}"></i>
                                </div>
                                <div class="test-card-stats">
                                    <div class="test-card-stat">
                                        <i class="fas fa-tasks"></i>
                                        <span>${test.actions.length} action${test.actions.length !== 1 ? 's' : ''}</span>
                                    </div>
                                    <div class="test-card-stat">
                                        <i class="fas fa-${test.stopOnFailure ? 'stop-circle' : 'play-circle'}"></i>
                                        <span>${test.stopOnFailure ? 'Stop on failure' : 'Continue on failure'}</span>
                                    </div>
                                </div>
                            </div>

                            <div class="test-card-actions">
                                <button class="${status === 'running' ? 'cancel-test-btn' : 'run-test-btn'}" data-index="${index}" data-testid="${status === 'running' && test.runId ? test.runId : test.id}">
                                    <i class="fas fa-${status === 'running' ? 'ban' : 'play'}"></i> ${status === 'running' ? 'Cancel Test' : 'Run Test'}
                                </button>
                                <button class="delete-test-btn" data-index="${index}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    `;
                container.appendChild(card);
            });
        };

        renderTestCards();

        container.addEventListener('click', async (e) => {
            const runButton = e.target.closest('.run-test-btn');
            const cancelButton = e.target.closest('.cancel-test-btn');
            const deleteButton = e.target.closest('.delete-test-btn');
            const actionsCount = e.target.closest('.test-card-stat');

            if (actionsCount && actionsCount.querySelector('i.fa-tasks')) {
                const card = actionsCount.closest('.test-card');
                const testId = card.dataset.testId;
                const test = builtTests.find(t => t.id === testId);

                if (test && test.actions) {
                    const popup = document.createElement('div');
                    popup.className = 'json-popup';
                    popup.innerHTML = `
                        <div class="popup-content">
                            <button class="popup-close">&times;</button>
                            <h3>Actions JSON</h3>
                            <pre>${JSON.stringify(test.actions, null, 2)}</pre>
                        </div>
                    `;

                    document.body.appendChild(popup);

                    popup.querySelector('.popup-close').addEventListener('click', () => {
                        document.body.removeChild(popup);
                    });
                }
            }

            if (runButton) {
                const button = runButton;
                if (button.disabled) return; // Prevent double clicks

                const index = button.dataset.index;
                const test = builtTests[index];
                const card = button.closest('.test-card');
                const testId = card.dataset.testId;
                const testToUpdate = builtTests.find(t => t.id === testId);
                const statusBadge = card.querySelector('.test-status-badge');

                // --- UI Update to "Starting" state ---
                button.disabled = true;
                button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Starting...';
                if (statusBadge) {
                    statusBadge.className = 'test-status-badge status-running';
                    statusBadge.innerHTML = '<i class="fas fa-spinner fa-spin"></i> RUNNING';
                }
                if (testToUpdate) {
                    testToUpdate.status = 'running';
                    sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                }

                showToast('Submitting test for execution...', 'info');

                try {
                    const res = await fetch('/api/tests/run', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(test)
                    });
                    const data = await res.json();

                    if (data.testRunId) {
                        // --- Test Started Successfully ---
                        if (testToUpdate) {
                            testToUpdate.runId = data.testRunId;
                            sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                        }
                        // Now transform the button into a real "Cancel" button
                        button.className = 'cancel-test-btn';
                        button.innerHTML = '<i class="fas fa-ban"></i> Cancel Test';
                        button.dataset.testid = data.testRunId; // Set the correct ID for cancellation
                        button.disabled = false; // Re-enable

                        pollForResults(data.testRunId);
                    } else {
                        // --- Test Failed to Start ---
                        showToast('Error starting test: ' + (data.message || JSON.stringify(data)), 'error');
                        if (testToUpdate) {
                            testToUpdate.status = 'pending';
                            delete testToUpdate.runId;
                            sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                            renderTestCards(); // Re-render to fix the card state
                        }
                    }
                } catch (err) {
                    // --- Network or other fetch error ---
                    showToast('Failed to run test.', 'error');
                    if (testToUpdate) {
                        testToUpdate.status = 'pending';
                        delete testToUpdate.runId;
                        sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                        renderTestCards(); // Re-render to fix the card state
                    }
                }
            } else if (cancelButton) {
                const button = cancelButton;
                const testRunId = button.dataset.testid;

                if (!/^[0-9]+$/.test(testRunId)) {
                    showToast('Cannot cancel, test is not running with a valid ID.', 'warning');
                    return;
                }

                button.disabled = true;
                button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Cancelling...';

                try {
                    const res = await fetch(`/api/tests/cancel/${testRunId}`, {method: 'POST'});
                    const data = await res.json();
                    if (data.cancelled) {
                        showToast('Test cancelled successfully.', 'warning');
                        const testToUpdate = builtTests.find(t => t.runId == testRunId);
                        if (testToUpdate) {
                            testToUpdate.status = 'cancelled';
                            delete testToUpdate.runId;
                            sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                            renderTestCards();
                        }
                    } else {
                        showToast('Failed to cancel test. It might have already completed.', 'error');
                        renderTestCards();
                    }
                } catch (err) {
                    showToast('Failed to send cancel request.', 'error');
                    renderTestCards();
                }
            } else if (deleteButton) {
                const button = deleteButton;
                const index = button.dataset.index;
                builtTests.splice(index, 1);
                sessionStorage.setItem('builtTests', JSON.stringify(builtTests));
                renderTestCards();
            }
        });

        const pollForResults = (testRunId) => {
            const interval = setInterval(async () => {
                const currentTests = JSON.parse(sessionStorage.getItem('builtTests') || '[]');
                const testToUpdate = currentTests.find(t => t.runId == testRunId);

                if (!testToUpdate) {
                    clearInterval(interval);
                    return;
                }

                try {
                    const res = await fetch(`/api/tests/results/${testRunId}`);
                    const data = await res.json();
                    if (data.status === 'finished' || data.status === 'not_found') {
                        clearInterval(interval);

                        const newStatus = (data.status === 'finished' && data.results && data.results.length > 0 && data.results.every(r => r.status === 'success')) ? 'pass' : 'fail';
                        const message = data.status === 'finished' ? `Test finished with status: ${newStatus.toUpperCase()}` : 'Test results not found, marked as failed.';
                        const messageType = newStatus === 'pass' ? 'success' : 'error';
                        showToast(message, messageType);

                        if (testToUpdate) {
                            testToUpdate.status = newStatus;
                            delete testToUpdate.runId;
                            sessionStorage.setItem('builtTests', JSON.stringify(currentTests));
                            builtTests = currentTests;
                            renderTestCards();
                        }
                    }
                } catch (err) {
                    clearInterval(interval);
                    showToast('Error polling for results.', 'error');
                    if (testToUpdate) {
                        testToUpdate.status = 'fail';
                        delete testToUpdate.runId;
                        sessionStorage.setItem('builtTests', JSON.stringify(currentTests));
                        builtTests = currentTests;
                        renderTestCards();
                    }
                }
            }, 2000);
        };
    };

    if (document.getElementById('test-runner-container')) {
        requestIdleCallback(initTestRunnerPage);
    }

    // Settings page initialization
    const settingsForm = document.getElementById('settings-form');
    if (settingsForm) {
        const loader = document.getElementById('settings-loader');
        const concurrencyInput = document.getElementById('concurrency');

        if (loader && concurrencyInput) {
            loader.style.display = 'block';
            settingsForm.style.display = 'none';

            fetch('/api/settings')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch settings');
                    }
                    return response.json();
                })
                .then(data => {
                    concurrencyInput.value = data.concurrency;
                    loader.style.display = 'none';
                    settingsForm.style.display = 'block';
                })
                .catch(error => {
                    console.error('Error loading settings:', error);
                    loader.innerHTML = '<p class="text-danger">Failed to load settings. Please try again later.</p>';
                    if (typeof showToast === 'function') {
                        showToast('Error loading settings. Please try again.', 'error');
                    }
                });
        }
    }

    // Test Data page initialization
    const initTestDataPage = () => {
        const createDataBtn = document.getElementById('create-data-btn');
        const dataEditorModal = document.getElementById('data-editor-modal');
        const deleteConfirmationModal = document.getElementById('delete-confirmation-modal');
        const dataSetForm = document.getElementById('data-set-form');
        const jsonEditor = document.getElementById('data-json-editor');
        const variablePreview = document.getElementById('variable-preview');

        if (!createDataBtn || !dataEditorModal || !dataSetForm) return;

        let currentEditingId = null;

        // Load and display existing data sets
        const loadDataSets = async () => {
            const loadingDiv = document.getElementById('loading-data-sets');
            const noDataDiv = document.getElementById('no-data-sets');
            const tableDiv = document.getElementById('data-sets-table');
            const tbody = document.getElementById('data-sets-tbody');

            try {
                const response = await fetch('/api/testdata');
                const dataSets = await response.json();

                loadingDiv.style.display = 'none';

                if (dataSets.length === 0) {
                    noDataDiv.style.display = 'block';
                    tableDiv.style.display = 'none';
                } else {
                    noDataDiv.style.display = 'none';
                    tableDiv.style.display = 'table';

                    tbody.innerHTML = dataSets.map(dataSet => {
                        const createdDate = new Date(dataSet.createdAt).toLocaleDateString();
                        const updatedDate = new Date(dataSet.updatedAt).toLocaleDateString();

                        return `
                                <tr>
                                    <td><strong>${escapeHtml(dataSet.name)}</strong></td>
                                    <td>${escapeHtml(dataSet.description || 'No description')}</td>
                                    <td>${createdDate}</td>
                                    <td>${updatedDate}</td>
                                    <td>
                                        <button class="action-btn btn-edit" onclick="editDataSet(${dataSet.id})">
                                            <i class="fas fa-edit"></i> Edit
                                        </button>
                                        <button class="action-btn btn-delete" onclick="deleteDataSet(${dataSet.id}, '${escapeHtml(dataSet.name)}')">
                                            <i class="fas fa-trash"></i> Delete
                                        </button>
                                    </td>
                                </tr>
                            `;
                    }).join('');
                }
            } catch (error) {
                console.error('Error loading data sets:', error);
                loadingDiv.innerHTML = '<p class="text-danger">Failed to load data sets. Please try again.</p>';
                showToast('Error loading data sets', 'error');
            }
        };

        // Extract variable names from JSON
        const extractVariables = (jsonString) => {
            try {
                const data = JSON.parse(jsonString);
                const variables = [];

                const extractFromObject = (obj, prefix = '') => {
                    for (const key in obj) {
                        const path = prefix ? `${prefix}.${key}` : key;
                        if (typeof obj[key] === 'object' && obj[key] !== null) {
                            extractFromObject(obj[key], path);
                        } else {
                            variables.push(`\${${path}}`);
                        }
                    }
                };

                extractFromObject(data);
                return variables;
            } catch {
                return ['Invalid JSON'];
            }
        };

        // Escape HTML to prevent XSS
        const escapeHtml = (text) => {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        };

        // Validate and preview JSON
        const validateAndPreviewJson = () => {
            const jsonText = jsonEditor.value.trim();
            const validIndicator = document.getElementById('json-valid');
            const invalidIndicator = document.getElementById('json-invalid');
            const errorMessage = document.getElementById('json-error-message');

            if (!jsonText) {
                validIndicator.style.display = 'none';
                invalidIndicator.style.display = 'none';
                variablePreview.innerHTML = '<p class="text-muted">Enter JSON data to see available variables</p>';
                return false;
            }

            try {
                const data = JSON.parse(jsonText);
                validIndicator.style.display = 'inline';
                invalidIndicator.style.display = 'none';

                // Generate variable preview
                const variables = extractVariables(jsonText);
                if (variables.length > 0 && variables[0] !== 'Invalid JSON') {
                    variablePreview.innerHTML = `
                            <div class="variable-preview-list">
                                ${variables.map(variable => `
                                    <div class="variable-item">
                                        <span class="variable-key">${variable}</span>
                                    </div>
                                `).join('')}
                            </div>
                        `;
                } else {
                    variablePreview.innerHTML = '<p class="text-muted">No variables found</p>';
                }
                return true;
            } catch (error) {
                validIndicator.style.display = 'none';
                invalidIndicator.style.display = 'inline';
                errorMessage.textContent = error.message;
                variablePreview.innerHTML = '<p class="text-danger">Fix JSON errors to see variable preview</p>';
                return false;
            }
        };

        // Event listeners
        jsonEditor.addEventListener('input', validateAndPreviewJson);

        createDataBtn.addEventListener('click', () => {
            currentEditingId = null;
            document.getElementById('modal-title').textContent = 'Create Data Set';
            document.getElementById('data-set-id').value = '';
            document.getElementById('data-set-name').value = '';
            document.getElementById('data-set-description').value = '';
            jsonEditor.value = '';
            validateAndPreviewJson();
            dataEditorModal.style.display = 'flex';
        });

        // Modal close functionality - updated to handle the new bin button
        document.querySelectorAll('.modal-close, .modal-close-btn, #cancel-btn, #cancel-delete-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                dataEditorModal.style.display = 'none';
                deleteConfirmationModal.style.display = 'none';
            });
        });

        // Form submission
        dataSetForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!validateAndPreviewJson()) {
                showToast('Please fix JSON errors before saving', 'error');
                return;
            }

            const formData = {
                name: document.getElementById('data-set-name').value,
                description: document.getElementById('data-set-description').value,
                dataJson: jsonEditor.value
            };

            const saveBtn = document.getElementById('save-btn');
            const originalText = saveBtn.innerHTML;
            saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
            saveBtn.disabled = true;

            try {
                const url = currentEditingId ? `/api/testdata/${currentEditingId}` : '/api/testdata';
                const method = currentEditingId ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    showToast(`Data set ${currentEditingId ? 'updated' : 'created'} successfully`, 'success');
                    dataEditorModal.style.display = 'none';
                    loadDataSets();
                } else {
                    throw new Error('Failed to save data set');
                }
            } catch (error) {
                console.error('Error saving data set:', error);
                showToast('Error saving data set', 'error');
            } finally {
                saveBtn.innerHTML = originalText;
                saveBtn.disabled = false;
            }
        });

        // Global functions for button clicks
        window.editDataSet = async (id) => {
            try {
                const response = await fetch(`/api/testdata/${id}`);
                const dataSet = await response.json();

                currentEditingId = id;
                document.getElementById('modal-title').textContent = 'Edit Data Set';
                document.getElementById('data-set-id').value = id;
                document.getElementById('data-set-name').value = dataSet.name;
                document.getElementById('data-set-description').value = dataSet.description || '';
                jsonEditor.value = dataSet.dataJson;
                validateAndPreviewJson();
                dataEditorModal.style.display = 'flex';
            } catch (error) {
                console.error('Error loading data set:', error);
                showToast('Error loading data set', 'error');
            }
        };

        window.deleteDataSet = (id, name) => {
            // Close the edit modal first if it's open
            dataEditorModal.style.display = 'none';

            // Then show the delete confirmation modal
            document.getElementById('delete-data-name').textContent = name;
            deleteConfirmationModal.style.display = 'flex';

            document.getElementById('confirm-delete-btn').onclick = async () => {
                try {
                    const response = await fetch(`/api/testdata/${id}`, {method: 'DELETE'});
                    if (response.ok) {
                        showToast('Data set deleted successfully', 'success');
                        deleteConfirmationModal.style.display = 'none';
                        loadDataSets();
                    } else {
                        throw new Error('Failed to delete data set');
                    }
                } catch (error) {
                    console.error('Error deleting data set:', error);
                    showToast('Error deleting data set', 'error');
                }
            };
        };

        // Initial load
        loadDataSets();
    };

    // Initialize Test Data page if elements are present
    if (document.getElementById('create-data-btn')) {
        requestIdleCallback(initTestDataPage);
    }
});

